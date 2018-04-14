import {Injectable} from '@angular/core';

@Injectable()
export class ClaimService {

    defaultClaim = {
        state: undefined,
        tosAccepted: false
    };

    public static state = {
        INCOMPLETE: 'INCOMPLETE',
        SUBMITTED: 'SUBMITTED',
        DISCARDED: 'DISCARDED'
    };
    /*
     * This is used to track front-end properties
     * of current claim
     */
    currentClaim;

    clearClaim() {
        this.currentClaim = this.defaultClaim;
    }

    setClaim(claim) {
        this.currentClaim = claim;
        this.currentClaim.tosAccepted = true;
    }

    createNewClaim() {
        this.currentClaim.state = ClaimService.state.INCOMPLETE;
    }

    hasIncompleteClaim() {
        return this.currentClaim ? this.currentClaim.state == ClaimService.state.INCOMPLETE : false;
    }

    submitCurrentClaim() {
        this.currentClaim.state = ClaimService.state.SUBMITTED;
    }

    discardCurrentClaim() {
        this.currentClaim.state = ClaimService.state.DISCARDED;
        this.currentClaim.tosAccepted = false;
    }

    acceptedTos() {
        return this.currentClaim.tosAccepted;
    }

    acceptTos(accepted) {
        this.currentClaim.tosAccepted = accepted;
    }
}
