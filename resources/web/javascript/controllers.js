var chatApp = angular.module('chatApp', []);
var socket = new WebSocket('ws://localhost:8081/');

function Conversation() {
    this.id = Math.floor((Math.random()*100000)+1);
    this.members = [];
    this.message = "";
    this.messages = [];

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
    $scope.nick = '';

    $scope.users = [
        {name: "Laurens"},
        {name: "Sophie"}
    ];

    $scope.addToConversation = function() {
        $rootScope.$emit('addMember', this.user);
        console.log('emit');
    }

    angular.element(document).ready(function() {
        var nick = "";
        while (nick == "") {
            nick = prompt("What would you like your chat name to be?");
            var i=0;
            while (i<$scope.users.length) {
                if (nick == $scope.users[i].name){
                    nick="";
                    break;
                }
                i++;
            }
            if (nick != ""  && nick != "you" && nick!="You" && nick!=null) {
                $scope.nick = nick;
                console.log($scope.users);
            } else {
                nick = "";
            };
        }
    });
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
        socket.send({conversation: $scope.active.id, member: user.name});
    });

    $scope.sendMessage = function() {
        if (this.conversation.message != '') {
            socket.send({text: this.conversation.message});
            console.log(this.conversation.message);
            this.conversation.messages.push({name: 'you', text: this.conversation.message});
            this.conversation.message = "";
        }
    }

    $scope.leaveConversation = function() {
        console.log("clicked leave conv");
    }
});
