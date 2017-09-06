'use strict';
var app = angular.module('vetafiApp');
app.controller('signDocumentPreviewCtrl', ['$scope', '$filter', '$stateParams', 'net', '$state', 'userValues',
    function ($scope, $filter, $stateParams, net, $state, userValues) {
        $scope.pages = _.range(1); // TODO get number of pages
        $scope.claimId = $stateParams.claimId;
        $scope.formId = $stateParams.formId;

        function currentDate() {
            return $filter('date')(new Date(), 'MM/dd/yyyy');
        }

        $scope.onSubmit = function () {
            userValues.date_signed = currentDate();
            userValues.signature = $scope.signature;

            net.saveForm(
              $scope.claimId,
              $scope.formId,
              userValues
            ).then(function (res) {
                claimService.createNewClaim();
                $state.transitionTo('root.sign', {claimId: $scope.claimId});
            });
        }


    }
]);
