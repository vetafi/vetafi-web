'use strict';
var app = angular.module('vetafiApp');
app.controller('signDocumentPreviewCtrl', ['$scope', '$filter', '$stateParams', '$state', 'net', '$uibModal',
    '$document', '$window',
    function ($scope, $filter, $stateParams, $state, claimForms, net, $uibModal, $document, $window) {
        self.pages = _.range(1)
    }
]);
