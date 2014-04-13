// User object
function User(ip, nickname) {
    this.ip = ip;
    this.nickname = nickname;
}

// Conversation object
function Conversation(user, removable) {
    this.id = Math.floor((Math.random()*100000)+1);
    this.user = user;
    this.active = 'active';
    this.removable = removable == undefined ? true : removable;

    this.message;
    this.messages = [];

    this.title = function() {
        return this.user.nickname;
    }

    this.equals = function(other) {
        return this.user === other.user;
    }
}

var chat = angular.module('chat', [])
// Provides functions for websocket communication
.service('websocketService', function($rootScope) {

})
// Provides application settings
.service('settingService', function($rootScope) {
    this.nickname = 'choose a nickname...';

    this.updateNickname = function(nickname) {
        this.nickname = nickname;
        $rootScope.$broadcast('settingService::nicknameChanged');
    }

    this.init = function() {
//        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
        $('#nick-modal form').on('submit', function(event) {
            $('#nick-modal').modal('hide');
        });
    }

    this.init();
})
// Provides functions to manipulate user model
.service('userModel', function(websocketService) {
    this.users = [
        new User('192.168.0.0', 'laurens'),
        new User('192.168.0.1', 'sophie')
    ];

})
// Provides function to manipulate conversation model
.service('conversationModel', function($rootScope, websocketService) {
    this.conversations = [new Conversation(new User('0.0.0.0', 'Everyone'), false)];

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
.controller('conversationController', function($scope, conversationModel) {
    $scope.conversations = conversationModel.conversations;

    $scope.sendMessage = function() {
        if (this.conversation.message != '') {
            this.conversation.messages.push({name: 'you', value: this.conversation.message});
            this.conversation.message = '';
            $('.tab-pane').animate({scrollTop: $('.tab-pane').height()+999999}, 'slow');
        }
    }

    $scope.closeConversation = function() {
        conversationModel.removeConversation(this.conversation);
    }

    $scope.$on('conversationModel::conversationsChanged', function() {
        $scope.conversations = conversationModel.conversations;
    });
});
