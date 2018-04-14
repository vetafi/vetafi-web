import {Component, Input, OnInit} from '@angular/core';
import {FormConfigService} from './formConfigService';
import {DatePipe} from '@angular/common';

const template = `
<div id="claim-view">
  <div id="claim-view-wrapper">
    <h3>Your Submitted Claim</h3>
    <div class="content general">
      <h4 class="content-header">General Info:</h4>
      <div class="line"><span>Submitted By:</span><span>{{user.firstname}} {{user.lastname}}</span></div>
      <div class="line"><span>Submitted Date:</span><span>{{claim.date}}</span></div>
    </div>
    <div class="content sent-to">
      <h4 class="content-header underline">Copies sent to:</h4>
      <div class="line" (click)="isEmailCollapsed = !isEmailCollapsed" [ngClass]="{'collapsed': isEmailCollapsed}">
        <div class="icon-wrapper">
          <div class="email-icon"></div>
        </div>Emails
        <div class="down-arrow-icon"></div>
      </div>
      <div class="collapse-wrapper email" uib-collapse="!isEmailCollapsed">
        <ul>
          <li *ngFor="let email of claim.sentTo.emails">{{email}}</li>
        </ul>
      </div>
      <div class="line" (click)="isAddressCollapsed = !isAddressCollapsed" [ngClass]="{'collapsed': isAddressCollapsed}">
        <div class="icon-wrapper">
          <div class="email-icon"></div>
        </div>Addresses
        <div class="down-arrow-icon"></div>
      </div>
      <div class="collapse-wrapper address" uib-collapse="!isAddressCollapsed">
        <ul>
          <li *ngFor="let address of claim.sentTo.addresses">
            <div><b>{{address.name}}</b></div>
            <div>{{address.street1}} {{address.street2}}</div>
            <div>{{address.city}}, {{address.province}}, {{address.country}}, {{address.postal}}</div>
          </li>
        </ul>
      </div>
    </div>
    <div class="content forms">
      <h4 class="content-header underline">Forms:</h4>
      <div class="form-wrapper" *ngFor="let form of claim.forms">
        <div class="icon-wrapper">
          <div class="form-icon"></div>
        </div>
        <div class="form-header">
          <div class="form-id">{{form.key}}</div>
          <div class="form-title">{{form.name}}</div>
        </div><a [href]="'/claim/' + claimId + '/form/' + form.key + '/pdf'">
          <button>
            <div class="download-icon"></div>Download
          </button></a>
      </div>
    </div>
  </div>
</div>

`;
@Component({
    selector: 'app-claim-view',
    template: template,
    styleUrls: ['../assets/styles/claimView.styl']
})
export class ClaimViewComponent implements OnInit {

    @Input('user') user;
    @Input('claim') claim;
    @Input('claimForms') claimForms;
    isEmailCollapsed: boolean = true;
    isAddressCollapsed: boolean = true;

    constructor(public formConfigService: FormConfigService,
                public datePipe: DatePipe) {

    }

    ngOnInit(): void {
        // Initialiaze form array
        this.claim.forms.forEach((form) => {
            form.name = this.formConfigService.getFormConfig()[form.key].vfi.title;
        });

        this.claim.date = this.datePipe.transform(new Date(this.claim.stateUpdatedAt), 'MM/dd/yyyy');
    }
}
