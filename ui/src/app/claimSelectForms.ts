import {Component, Input, OnInit} from '@angular/core';
import {WindowRef} from './window';
import {ClaimService} from './claimService';
import {FormConfigService} from './formConfigService';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ActivatedRoute, Router} from '@angular/router';
import sum from 'lodash/sum';
import keyBy from 'lodash/keyBy';

const template = `
<div id="select-forms-view">
  <app-breadcrumbs page="1"></app-breadcrumbs>
  <div id="select-forms-wrapper">
    <h3>Fill Forms for Claim</h3>
    <div class="section required">
      <div class="section-header">
        <h4>Required Forms:</h4>
        <ngb-progressbar class="progressbar" 
        [value]="numRequiredCompleted" 
        [max]="numRequiredForms" 
        type="{{numRequiredCompleted == numRequiredForms ? 'success' : 'info'}}">
            <span class="progress-bar-text">{{numRequiredCompleted}}/{{numRequiredForms}}</span>
        </ngb-progressbar>
      </div>
      <div class="form-wrapper" *ngFor="let formId of myFormIds">
        <div class="form-content">
          <div class="state-wrapper" [ngClass]="{'required': formConfig[formId].vfi.required, 'complete': isCompletedForm(myForms[formId])}">
            <div class="state-icon"></div>
          </div>
          <div class="form-title-wrapper">
            <h5 class="title" [ngClass]="{'required': formConfig[formId].vfi.required && !isCompletedForm(myForms[formId])}">{{formId}}</h5>
            <h5 class="warning required" *ngIf="formConfig[formId].vfi.required && !isCompletedForm(myForms[formId])">&nbsp;(required)</h5>
            <h5 class="subtitle">{{formConfig[formId].vfi.title}}</h5>
          </div>
          <div class="form-summary">{{formConfig[formId].vfi.summary}}</div>
        </div>
        <div class="form-buttons">
          <button class="edit-btn" routerLink="/claim/{{claimId}}/form/{{formId}}">
            <div class="edit-icon"></div>Edit
          </button>
          <button class="download-btn" type="button" (click)="onDownload(formId)">
            <div class="download-icon"></div>Download
          </button>
        </div>
      </div>
    </div>
    <div class="button-wrapper">
      <button class="cancel-btn" (click)="onClickCancel()">Cancel</button>
      <button class="done-btn" routerLink="/sign/{{claimId}}" [ngClass]="{'ready':numRequiredCompleted == numRequiredForms}">Done</button>
      <div class="clearer"></div>
    </div>
  </div>
</div>
<app-footer></app-footer>
`;

@Component({
    selector: 'app-claim-select-forms',
    template: template,
    styleUrls: ['../assets/styles/claimSelectForms.styl']
})
export class ClaimSelectFormsComponent implements OnInit {

    busySpinner;
    claimForms;
    claimId;
    myForms: any;
    formConfig: any;
    numRequiredCompleted: any;
    numRequiredForms: any;
    myFormIds: string[];

    constructor(public windowRef: WindowRef,
                public claimService: ClaimService,
                public formConfigService: FormConfigService,
                public router: Router,
                public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.claimForms = this.activatedRoute.snapshot.data.claimForms;
        this.claimId = this.activatedRoute.snapshot.params.claimId;
        // claimForms is an array of form objects associated with claim
        // myForms is a mapping of formId -> claimForm object
        this.myForms = keyBy(this.claimForms, function (form) {
            return form.key;
        });

        this.myFormIds = this.claimForms.map((x) => x.key);

        console.log(this);

        // All available forms
        this.formConfig = this.formConfigService.getFormConfig();
        console.log(this.formConfig);

        this.numRequiredCompleted = sum(this.claimForms.map((form: any) => {
            return form && form.answeredRequired == form.requiredQuestions ? 1 : 0;
        }));
        this.numRequiredForms = sum(this.claimForms.map((form: any) => {
            return this.formConfig[form.key].vfi.required ? 1 : 0;
        }));
    }

    onDownload(formId: String) {
        this.windowRef.nativeWindow.open('/pdf/' + this.claimId + '/' + formId, '_blank');
        this.busySpinner.showBusyUntilDownload();
    }

    onClickCancel() {
        this.router.navigateByUrl('/home');
    }

    isCompletedForm(myForm): boolean {
        return myForm && myForm.answeredRequired > 0 &&
            myForm.answeredRequired >= myForm.requiredQuestions;
    }
}
