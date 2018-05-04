import {Component, OnInit} from '@angular/core';
import {DatePipe} from "@angular/common";
import {ClaimService} from './claimService';
import {AjaxService} from './net';
import {ActivatedRoute, Router} from '@angular/router';
import {FormConfigService} from './formConfigService';


const template = `
<div id="vfi-claim-confirm-view">
  <app-breadcrumbs page="3"></app-breadcrumbs>
  <div id="claim-confirm-wrapper">
    <h2 class="header">Confirm Claim Submission</h2>
    <div class="section intro">
      <h4>Hi {{user.firstname}}!</h4>
      <div class="row">
        <div class="col-sm-6">
          <p>
            You are one step away from submitting your claim.
            Please look over your submission and click on Submit if everything is correct.
          </p>
        </div>
        <div class="col-sm-6">
          <button class="confirm-btn" (click)="onClickConfirm()">Submit</button>
        </div>
      </div>
    </div>
    <div class="section basic-info">
      <h4>General</h4>
      <div class="basic-row"><span>Name:</span><span>{{user.firstname}} {{user.lastname}}</span></div>
      <div class="basic-row"><span>Today's date:</span><span>{{dateToday}}</span></div>
      <div class="basic-row"><span>Number of forms:</span><span>{{forms.length}}</span></div>
    </div>
    <div class="section mailing-list">
      <div class="section-header">
        <h4>Fax form to:</h4>
      </div>
      <div class="container-fluid">
        <div class="row-wrapper row">
          <h5 class="title col-sm-5"><b>VA Claims Intake Center</b></h5>
          <div class="address col-sm-5">
            <div class="address-line">844-531-7818</div>
          </div>
        </div>
      </div>
    </div>
    <div class="section email-list">
      <div class="section-header">
        <h4>E-mail electronic copies to:</h4>
      </div>
      <div class="container-fluid">
        <div class="row-wrapper row">
          <div class="title col-sm-5">{{userEmail}}</div>
        </div>
      </div>
    </div>
    <div class="section forms-list">
      <div class="section-header">
        <h4>Forms:</h4>
        <button routerLink="/claim/{{claimId}}/select-forms">
          <div class="edit-icon"></div>Review Forms
        </button>
      </div>
      <div class="container-fluid">
        <div class="row-wrapper row" *ngFor="let form of forms">
          <h5 class="title col-sm-5"><b>{{formsConfig[form.key].vfi.title}}</b></h5>
          <div class="form-description col-sm-7">{{formsConfig[form.key].vfi.summary}}</div>
        </div>
      </div>
    </div>
    <div class="button-wrapper">
      <button class="back-btn" routerLink="/claim/{{claimId}}/select-forms">Back</button>
      <button class="confirm-btn" (click)="onClickConfirm()">Submit</button>
    </div>
  </div>
</div>
<app-footer></app-footer>

<div id="busy-overlay" *ngIf="loading">
   <img class="busy-spinner" src="/assets/icons/spinner.svg">
</div>
`;


@Component({
    selector: 'app-claim-confirm',
    template: template,
    styleUrls: ['../assets/styles/claimConfirm.styl']
})
export class ClaimConfirmComponent implements OnInit {

    public loading: boolean = false;
    claimId;
    user;
    userEmail;
    userAddress;
    emailList;
    dateToday;
    forms;
    formsConfig;

    constructor(public datePipe: DatePipe,
                public claimService: ClaimService,
                public ajaxService: AjaxService,
                public router: Router,
                public activatedRoute: ActivatedRoute,
                public formConfigService: FormConfigService) {
    }


    ngOnInit(): void {
        console.log(this.activatedRoute.snapshot.data);
        this.formsConfig = this.formConfigService.getFormConfig();
        this.forms = this.activatedRoute.snapshot.data.claimForms;
        this.claimId = this.activatedRoute.snapshot.params.claimId;
        this.user = this.activatedRoute.snapshot.data.user;
        this.userEmail = this.user.email;
        this.userAddress = this.user.contact.address;
        this.emailList = [
            {
                name: 'Me',
                email: this.userEmail
            }
        ];
        this.dateToday = this.datePipe.transform(new Date(), 'MM/dd/yyyy');
    }

    onClickConfirm() {
        this.loading = true;
        this.ajaxService.submitClaim(this.claimId, [])
            .subscribe((resp) => {
                if (resp) {
                    this.claimService.submitCurrentClaim();
                    this.loading = false;
                    this.router.navigateByUrl(
                        "claim/" + this.claimId + "/submit");
                } else {
                    this.loading = false;
                    this.router.navigateByUrl(
                        "claim/" + this.claimId + "/submit");
                }
            }, (data) => {
                this.loading = false;
                this.router.navigateByUrl(
                    "claim/" + this.claimId + "/submit");
            });
    }
}
