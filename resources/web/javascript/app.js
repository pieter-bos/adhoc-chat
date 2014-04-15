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
    var self = this;
    this.socket = new WebSocketJSONRPC("ws://localhost:8081/")
        .def('getConversations')
        .def('getUsers')
        .def('updateNickname')
        .def('sendMessage')
        .def('subscribe');

    this.socket.subscribe('newConversation');

    this.updateNickname = function(nickname) {
        this.socket.updateNickname(nickname, function(e) {
            if (e != '') {
                $rootScope.$broadcast('websocketService::nickChanged', e);
            }
        });
    }

    this.sendMessage = function(message, convId) {
        console.log(convId);
        this.socket.sendMessage(message, convId, function(e) {
            var m = JSON.parse(e);
            console.log(m);
            $rootScope.$broadcast('websocketService::newMessage', m.convId, m.message, m.nickname);
        });
    }

    this.socket.on('newConversation', function(data) {
        console.log('new:');
    });

    this.socket.on('open', function() {
        self.socket.getConversations(function(e) {
            $rootScope.$broadcast('websocketService::newConversation', e);
        });

        self.socket.getUsers(function(e) {
            JSON.parse(e).forEach(function(element) {
                console.log(element);
                $rootScope.$broadcast('websocketService::newUser', element);
            });
        });
    });
})
// Provides application settings
.service('settingService', function($rootScope, websocketService) {
    this.nickname = 'choose a nickname...';

    this.updateNickname = function(nickname) {
        websocketService.updateNickname(nickname);
    }

    $rootScope.$on('websocketService::nickChanged', function(event, nick) {
        this.nickname = nick;
        $rootScope.$broadcast('settingService::nicknameChanged');
    });

    this.init = function() {
        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
        $('#nick-modal form').on('submit', function(event) {
            $('#nick-modal').modal('hide');
        });
    }

    this.init();
})
// Provides functions to manipulate user model
.service('userModel', function($rootScope, websocketService) {
    var self = this;
    this.users = [];

    $rootScope.$on('websocketService::newUser', function(event, user) {
        self.users.push(user);
        $rootScope.$broadcast('userModel::usersChanged');
    });
})
// Provides function to manipulate conversation model
.service('conversationModel', function($rootScope, websocketService) {
    var self = this;
    this.conversations = [];

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

    $rootScope.$on('websocketService::newConversation', function(event, data) {
        var removable = data.user === undefined ? false : true;
        var user = data.user === undefined ? 'Everyone' : data.user;
        var conv = new Conversation(user, removable);
        conv.id = data.id;
        conv.messages = data.messages;
        self.conversations.push(conv);

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
        $scope.nickname = nickname;
        $scope.$apply();
    });
})
// Controller for user related views
.controller('userController', function($scope, userModel, conversationModel) {
    $scope.users = userModel.users;

    $scope.startConversation = function() {
        conversationModel.startConversation(this.user);
    }

    $scope.$on('userModel::usersChanged', function() {
        $scope.users = userModel.users;
        $scope.$apply();
    });
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
