$.ready(function() {
    var nick = prompt("Please enter your name that you'd like to use for the chat.");
    if (nick != null) {
        $scope.nick = nick;
    }
})