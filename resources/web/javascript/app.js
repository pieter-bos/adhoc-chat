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
        $('#nick-modal').modal({ keyboard: false, backdrop: 'static' });
        $('#nick-modal form').on('submit', function(event) {
            $('#nick-modal').modal('hide');
        });
    }

    this.init();
})
// Provides functions to manipulate user model
.service('userModel', function(websocketService) {
    function User(ip, nickname) {
        this.ip = ip;
        this.nickname = nickname;
    }

    this.users = [
        new User('192.168.0.0', 'laurens'),
        new User('192.168.0.1', 'sophie')
    ];

})
// Provides function to manipulate conversation model
.service('conversationModel', function(websocketService) {

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

    }
})
// Controller for conversation related views
.controller('conversationController', function($scope, conversationModel) {

});
