import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, RouterStateSnapshot} from '@angular/router';
import {AjaxService} from './net';
import {ClaimService} from './claimService';
import isEmpty from 'lodash/isEmpty'
import find from 'lodash/find'

@Injectable()
export class ClaimsResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService, private claimService: ClaimService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        return this.ajaxService.getClaimsForUser().pipe(
            (res) => {
                let existingClaim = find(res, {'state': 'INCOMPLETE'});
                if (!isEmpty(existingClaim)) {
                    this.claimService.setClaim(existingClaim);
                }
                return res;
            }
        ).toPromise().catch(
            (err) => {
                return null;
            }
        );
    }
}

@Injectable()
export class ClaimResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        console.log(route);
        return this.ajaxService.getClaim(route.params.claimId);
    }
}

@Injectable()
export class FormsResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        console.log(route);
        return this.ajaxService.getFormsForClaim(route.params.claimId);
    }
}

@Injectable()
export class UserResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        return this.ajaxService.getUserInfo().toPromise()
            .catch((err) => {
                console.error(err);
                return null;
            });
    }
}

@Injectable()
export class MaybeUserResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        return this.ajaxService.getUserInfo().toPromise()
            .catch((err) => {
                console.error(err);
                return null;
            });
    }
}

@Injectable()
export class UserValuesResolve implements Resolve<any> {

    constructor(private ajaxService: AjaxService) {}

    resolve(route: ActivatedRouteSnapshot,
            state: RouterStateSnapshot) {
        return this.ajaxService.getUserValues();
    }
}

