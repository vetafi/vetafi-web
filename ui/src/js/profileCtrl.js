'use strict';
var app = angular.module('vetafiApp');
app.controller('profileCtrl', ['$scope', '$location', '$window', 'Profile', 'claimService', 'net',
    '$uibModal', '$state', 'userClaims', '$filter', 'claimConfig',
  function($scope, $location, $window, Profile, claimService, net,
           $uibModal, $state, userClaims, $filter, claimConfig) {
    $scope.user = Profile.user;
    $scope.claims = userClaims;
    $scope.claimConfig = _.keyBy(claimConfig.claims, 'key');
    console.log(userClaims);

    function createHeaderString(claim) {
      if (claim.state == 'INCOMPLETE') {
        return 'Started (incomplete)';
      } else if (claim.state == 'SUBMITTED') {
        return 'Submitted';
      }
    }

    function init() {
      for (var i = 0; i < $scope.claims.length; i++) {
        $scope.claims[i].id = $scope.claims[i].claimID;
        $scope.claims[i].header = createHeaderString($scope.claims[i]);
        $scope.claims[i].date = $filter('date')(new Date($scope.claims[i].stateUpdatedAt), 'MM/dd/yyyy');
      }
    }
    init();

    $scope.clickEditInfo = function() {
      var newScope = $scope.$new(true);
      newScope.headline = "Edit General Information";
      var modalInstance = $uibModal.open({
        scope: newScope,
        templateUrl: 'templates/modals/editProfile.html',
        windowClass: 'ngdialog-theme-default',
        controller: 'editProfileCtrl',
        resolve: {
          user: function () {
            return $scope.user;
          }
        }
      });
      modalInstance.result.then(function (user) {
        $scope.user = user;
      }, function () {
        console.log('modal dismissed');
      });
    };

    $scope.clickChangePassword = function() {
      var newScope = $scope.$new(true);
      newScope.headline = "Change Password";
      $uibModal.open({
        scope: newScope,
        templateUrl: 'templates/modals/changePassword.html',
        windowClass: 'ngdialog-theme-default'
      });
    };

    $scope.clickLinkIdMe = function() {
      // Redirect to IdMe auth page, which will redirect to the specified uri setup with IdMe.
      $window.location.href = net.getAuthIdMeUrl();
    };

    $scope.clickDeleteAccount = function() {
      var newScope = $scope.$new(true);
      newScope.headline = "Delete Account";
      newScope.message = "Are you sure you want to delete your account? All your saved personal information will be lost.";
      newScope.choice = 'warning';
      newScope.continueText = 'Delete';
      var modal = $uibModal.open({
        scope: newScope,
        templateUrl: 'templates/modals/twoButtonModal.html',
        windowClass: 'ngdialog-theme-default'
      });
      modal.result.then(function() {
        net.deleteUserAccount().then(function(resp) {
          Profile.logout();
          if (resp.status == 200) {
            $location.path('/');
          }
        });
      });
    };
  }
]);
