var chatApp = angular.module('chatApp', []);
var socket = new WebSocket('ws://localhost:8081/');

function Conversation() {
    this.id = Math.floor((Math.random()*100000)+1);
    this.members = [];

    this.title = function() {
        return this.id;
    }

    this.addMember = function(user) {
        this.members.push(user);
        console.log(this.id);
        console.log(this.members);
    }
}

chatApp.controller('userController', function ($scope, $rootScope) {
    $scope.users = [
        {name: 'Laurens'},
        {name: 'Sophie'}
    ];

    $scope.addToConversation = function() {
        $rootScope.$emit('addMember', this.user);
        console.log('emit');
    }
});

chatApp.controller('conversationController', function($scope, $rootScope) {
    $scope.conversations = [
        new Conversation()
    ];
    $scope.active = $scope.conversations[0];

    $scope.addConversation = function() {
        $scope.conversations.push(new Conversation());
    }

    $scope.setActive = function() {
        $scope.active = this.conversation;
        console.log($scope.active.id);
    }

    $rootScope.$on('addMember', function(e, user) {
        $scope.active.addMember(user);
        console.log('on');
    });
});
