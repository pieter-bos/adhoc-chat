// Conversation object
function Conversation(user, removable) {
    this.id = Math.floor((Math.random()*100000)+1);
    this.user = user;
    this.active = 'active';
    this.removable = removable == undefined ? true : removable;

    this.message;
    this.messages = [];

    this.title = function() {
        return this.user;
    }

    this.equals = function(other) {
        return this.user === other.user;
    }
}

var chat = angular.module('chat', [])
// Provides functions for websocket communication
.service('websocketService', function($rootScope) {
    this.socket = new WebSocketJSONRPC("ws://localhost:8081/")
        .def('updateNickname')
        .def('sendMessage');

    this.updateNickname = function(nickname) {
        this.socket.updateNickname(nickname, function(e) { console.log(e); });
    }

    this.sendMessage = function(message, convId) {
        console.log(convId);
        this.socket.sendMessage(message, convId, function(e) {
            var m = JSON.parse(e);
            console.log(m);
            $rootScope.$broadcast('websocketService::newMessage', m.convId, m.message, m.nickname);
        });
    }
})
// Provides application settings
.service('settingService', function($rootScope, websocketService) {
    this.nickname = 'choose a nickname...';

    this.updateNickname = function(nickname) {
        websocketService.updateNickname(nickname);
        this.nickname = nickname;
        $rootScope.$broadcast('settingService::nicknameChanged');
    }

    this.init = function() {
        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
        $('#nick-modal form').on('submit', function(event) {
            $('#nick-modal').modal('hide');
        });
    }

    this.init();
})
// Provides functions to manipulate user model
.service('userModel', function(websocketService) {
    this.users = [];
})
// Provides function to manipulate conversation model
.service('conversationModel', function($rootScope, websocketService) {
    var self = this;
    this.conversations = [new Conversation('Everyone', false)];

    // Adds a new conversation to the list and makes all other conversations inactive
    this.addConversation = function(conv) {
        for (var i = 0; i < this.conversations.length; i++) {
            this.conversations[i].active = '';
        }

        this.conversations.push(conv);
    }

    // Removes a conversation and makes the default conversation active
    this.removeConversation = function(conv) {
        this.conversations.splice(this.conversations.indexOf(conv), 1);
        this.conversations[0].active = 'active';
        $rootScope.$broadcast('conversationModel::conversationsChanged');
    }

    // Starts a new conversation with the user if there is no current conversation with the user
    this.startConversation = function(user) {
        var newConv = new Conversation(user);

        for (var i = 0; i < this.conversations.length; i++) {
            if (this.conversations[i].equals(newConv)) {
                return;
            }
        }

        this.addConversation(newConv);
        $rootScope.$broadcast('conversationModel::conversationsChanged');
    }

    $rootScope.$on('websocketService::newMessage', function(event, id, message, nickname) {
        console.log(self);

        for (var i = 0; i < self.conversations.length; i++) {
            if (self.conversations[i].id === id) {
                self.conversations[i].messages.push({name: nickname, value: message});
                console.log(self.conversations[i]);
            }
        }

        $rootScope.$broadcast('conversationModel::conversationsChanged');
    });
})
// Controller for application settings
.controller('settingController', function($scope, settingService) {
    $scope.nickname = settingService.nickname;

    $scope.nicknameField = '';

    $scope.updateNickname = function() {
        settingService.updateNickname($scope.nicknameField);
        $scope.nicknameField = '';
    }

    $scope.$on('settingService::nicknameChanged', function() {
        $scope.nickname = settingService.nickname;
    });
})
// Controller for user related views
.controller('userController', function($scope, userModel, conversationModel) {
    $scope.users = userModel.users;

    $scope.startConversation = function() {
        conversationModel.startConversation(this.user);
    }
})
// Controller for conversation related views
.controller('conversationController', function($scope, conversationModel, websocketService) {
    $scope.conversations = conversationModel.conversations;

    $scope.sendMessage = function() {
        if (this.conversation.message != '') {
            websocketService.sendMessage(this.conversation.message, this.conversation.id);
            this.conversation.message = '';
        }
    }

    $scope.closeConversation = function() {
        conversationModel.removeConversation(this.conversation);
    }

    $scope.$on('conversationModel::conversationsChanged', function() {
        $scope.conversations = conversationModel.conversations;
        $scope.$apply();
    });
});
