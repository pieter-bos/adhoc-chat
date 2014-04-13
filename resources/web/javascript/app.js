var chat = angular.module('chat', [])
// Provides functions for websocket communication
.service('websocketService', function($rootScope) {

})
// Provides functions to manipulate user model
.service('userModel', function(websocketService) {

})
// Provides function to manipulate conversation model
.service('conversationModel', function(websocketService) {

})
// Controller for user related views
.controller('userController', function($scope, userModel) {

})
// Controller for conversation related views
.controller('conversationController', function($scope, conversationModel) {

});
