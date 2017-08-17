'use strict';
var app = angular.module('vetafiApp');
app.controller('signDocumentCtrl', ['$scope', '$filter', '$stateParams', '$state', 'claimForms', 'net', '$uibModal',
    '$document', '$window',
    function ($scope, $filter, $stateParams, $state, claimForms, net, $uibModal, $document, $window) {
        $scope.forms = claimForms;
        $scope.currentFormIndex = 0;
        $scope.allFormsSigned = testAllFormsSigned();
        $scope.showPrompt = false;
        $scope.claimId = $stateParams.claimId;

        function testAllFormsSigned() {
            var result = true;
            for (var i = 0; i < $scope.forms.length; i++) {
                result = result && $scope.forms[i].isSigned
            }
            return result;
        }

        function handleVisibilityChange() {
            if (!document.hidden) {
                for (var i = 0; i < $scope.forms.length; i++) {
                    var form = $scope.forms[i];
                    net.getFormSignatureStatus(form.claimID, form.key).then(
                        function success(res) {
                            form.isSigned = res.data;
                            $scope.allFormsSigned = testAllFormsSigned();
                        })
                }
            }
        }

        $window.addEventListener("visibilitychange",
            handleVisibilityChange,
            false);

        $scope.markCurrentlySigningForm = function (form) {
            $scope.currentlySigningForm = form
        };
    }
]);
