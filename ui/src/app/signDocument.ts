import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';

const template = `
<div id="vfi-sign-document-view">
  <app-breadcrumbs [page]="2"></app-breadcrumbs>
  <div id="sign-document-wrapper">
    <h2 class="header">Sign Documents</h2>
    <div class="section">
      <div class="container-fluid nopadding">
        <div class="row">
          <div class="col-xs-8">
            <p>Please sign the documents below.</p>
            <ul>
              <li>Click the link that says "Sign" for each document. Our secure eSignature platform will open in a new window with the completed document.</li>
              <li>After signing, close the window and return to this page.</li>
              <li>Repeat for all documents.</li>
            </ul>
          </div>
          <div class="col-xs-4">
            <button class="complete" [ngClass]="{'disabled': !allFormsSigned}" routerLink="/claim/{{claimId}}/confirm">Done</button>
          </div>
        </div>
      </div>
    </div>
    <div class="section">
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-4">
            <h4>Document Name</h4>
          </div>
          <div class="col-md-4">
            <h4>Status</h4>
          </div>
        </div>
        <div class="row" *ngFor="let form of forms">
          <div class="col-md-4 signature-table-element">
            <p>{{form.key}}</p>
          </div>
          <div class="col-md-4 signature-table-element">
            <div class="signature-complete-indicator" [hidden]="!form.isSigned">
              <p *ngIf="form.isSigned">Signed</p><img src="/assets/icons/checkmark-green.svg"/>
            </div>
            <p *ngIf="!form.isSigned">Not Signed</p>
          </div>
          <div class="col-md-4">
          <a class="btn" [ngClass]="{'disabled': form.isSigned}" routerLink="/sign/{{claimId}}/{{form.key}}">Sign</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<app-footer></app-footer>
`;

@Component({
    selector: 'app-sign-document',
    template: template,
    styleUrls: ['../assets/styles/signDocument.styl']
})
export class SignDocumentComponent implements OnInit {

    forms;
    claimId;
    allFormsSigned: boolean;

    constructor(private activatedRoute: ActivatedRoute) {
    }

    ngOnInit(): void {
        console.log(this.activatedRoute.snapshot.data);
        this.forms = this.activatedRoute.snapshot.data.claimForms;
        this.claimId = this.activatedRoute.snapshot.params.claimId;
        this.allFormsSigned = this.testAllFormsSigned();
    }

    testAllFormsSigned(): boolean {
        let result = true;
        for (let i = 0; i < this.forms.length; i++) {
            result = result && this.forms[i].isSigned;
        }
        return result;
    }
}
