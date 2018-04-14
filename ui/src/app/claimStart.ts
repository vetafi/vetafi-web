import {Component, OnInit} from '@angular/core';
import {AjaxService} from './net';
import {ClaimService} from './claimService';
import {ClaimConfigService} from './claimConfigService';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {FormConfigService} from './formConfigService';
import {TosModalComponent} from './tos';
import {ActivatedRoute, Router} from '@angular/router';

const template = `
<div id="claim-start-view">
  <app-breadcrumbs [page]="0"></app-breadcrumbs>
  <div id="claim-start-wrapper">
    <h3>Select Claim Type</h3>
    <div class="content container" *ngIf="isSignedIn">
      <div class="row">
        <div class="col-sm-2">Claim
          <hr/>
        </div>
        <div class="col-sm-5">Description
          <hr/>
        </div>
        <div class="col-sm-3">Number of Forms
          <hr/>
        </div>
      </div>
      <div class="row" *ngFor="let claim of claimConfig.claims">
        <div class="col-sm-2">{{claim.name}}</div>
        <div class="col-sm-5">{{claim.description}}</div>
        <div class="col-sm-3">{{claim.forms.length}}</div>
        <div class="col-sm-2">
          <button (click)="startClaim(claim)">Select</button>
        </div>
      </div>
    </div>
    <div class="proceed-wrapper" *ngIf="!isSignedIn">
      <div>
        <h4 class="signup">You must sign into an account to continue</h4><a href="/signup">
          <button class="signup-btn">Sign Up</button></a>
      </div>
    </div>
  </div>
</div>
<app-footer></app-footer>
`;


@Component({
    selector: 'app-claim-start',
    template: template,
    styleUrls: ['../assets/styles/claimStart.styl']
})
export class ClaimStartComponent implements OnInit {

    isSignedIn: boolean;
    claimConfig;
    formConfig;
    claimForms;
    user;
    userClaims;

    ngOnInit(): void {
        this.isSignedIn = !!this.route.snapshot.data.user;
        this.claimConfig = ClaimConfigService.getClaimConfig();
        this.formConfig = this.formConfigService.getFormConfig();
        this.user = this.route.snapshot.data.user;
        this.isSignedIn = this.user != null;
        let userClaims = this.route.snapshot.data.userClaims;
        let incompleteClaims = userClaims.filter(
            c => c.state != 'SUBMITTED' && c.state != 'DISCARDED');
        if (incompleteClaims > 0) {
            this.claimService.setClaim(incompleteClaims[0]);
        } else {
            this.claimService.clearClaim();
        }
        console.log(this.user);
    }

    constructor(public ajaxService: AjaxService,
                public claimService: ClaimService,
                public ngbModal: NgbModal,
                public formConfigService: FormConfigService,
                public router: Router,
                private route: ActivatedRoute) {

    }

    private goStartClaim(claim) {
        this.ajaxService.startClaim(claim).subscribe(
            (res) => {
                this.claimService.createNewClaim();
                this.router.navigateByUrl(
                    'claim/' + res.claimID + '/select-forms');
            }
        );
    }

    startClaim(claim) {
        if (!this.claimService.acceptedTos()) {
            const modalRef: NgbModalRef = this.ngbModal.open(TosModalComponent);
            modalRef.result.then(
                (res) => {
                    this.claimService.acceptTos(true);
                    this.goStartClaim(claim);
                }
            );
        } else {
            this.goStartClaim(claim);
        }
    }
}
