import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component, Inject, OnInit} from '@angular/core';
import {ClaimConfigService} from "./claimConfigService";
import {AjaxService} from "./net";
import {EditProfileComponent} from "./modalEditProfile";
import {Location, LocationStrategy, PathLocationStrategy} from '@angular/common';
import keyBy from 'lodash/keyBy';
import {ActivatedRoute, Router} from '@angular/router';
import { DOCUMENT } from '@angular/common';


const deleteAccountTemplate = `
<div class="vfi-modal">
  <div class="modal-header">
    <h4 class="modal-title">Delete Account</h4>
    <button type="button" class="close" aria-label="Close" (click)="activeModal.dismiss()">
      <span aria-hidden="true">&times;</span>
    </button>
  </div>
  <div id="basic-two-button-modal" class="modal-body">
    <div class="message">
      <p>Are you sure you want to delete your account? All your saved personal information will be lost.</p>
    </div>
  </div>
  <div class="button-wrapper modal-footer">
      <button class="cancel" (click)="activeModal.dismiss()">
        Cancel
      </button>
      <button class="continue warning" (click)="activeModal.close(true)">
        Delete
      </button>
  </div>
</div>
`;
@Component(
    {
        selector: "delete-account-modal",
        template: deleteAccountTemplate
    }
)
export class DeleteAccountModal {

    constructor(public activeModal: NgbActiveModal) {

    }
}



const generalTemplate = `
<div class="profile-section" id="general">
  <h4>Name</h4>
  <div class="content" *ngIf="user.firstname">{{user.firstname}} {{user.middlename}} {{user.lastname}}</div>
  <div class="content missing" *ngIf="!user.firstname">(Missing)</div>
  <h4>Primary Email</h4>
  <div class="content" *ngIf="user.email">{{user.email}}</div>
  <div class="content missing" *ngIf="!user.email">(Missing)</div>
  <h4>Primary Address</h4>
  <div class="content" *ngIf="user.contact">
    <div class="address-line">{{user.contact.address.name}}</div>
    <div class="address-line">{{user.contact.address.street1}}</div>
    <div class="address-line">{{user.contact.address.street2}}</div>
    <div class="address-line">{{user.contact.address.city}}, {{user.contact.address.province}} {{user.contact.address.country}} {{user.contact.address.postal}}</div>
  </div>
  <div class="content missing" *ngIf="!user.contact">(Missing)</div>
  <div class="button-wrapper">
    <button (click)="clickEditInfo()">
      <div class="edit-icon"></div>Edit
    </button>
  </div>
</div>
`;

@Component({
    selector: 'app-profile-general',
    template: generalTemplate
})
export class ProfileGeneralComponent implements OnInit {

    public user;

    constructor(public ajaxService: AjaxService,
                public ngbModal: NgbModal,
                public activatedRoute: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.user = this.activatedRoute.snapshot.data.user;
        console.log(this.user);
    }


    clickEditInfo(): void {
        this.ngbModal.open(EditProfileComponent).result.then(
            (user) => {
                this.user = user
            }
        );
    }
}

const claimsTemplate = `
<div class="profile-section" id="claims">
  <div class="no-claims-wrapper" *ngIf="claims.length == 0">
    <h4>You have not begun any claims yet.</h4><br/>
    <p>Click <a routerLink="/claim/start">here</a> to start a file claim.</p>
  </div>
  <div class="claims-wrapper" *ngIf="claims.length > 0">
    <h3>Your Claims</h3>
    <div class="claim-card" *ngFor="let claim of claims" [ngClass]="{'edit': claim.state=='incomplete', 'submitted': claim.state=='submitted'}">
      <div class="button-wrapper">
        <button class="edit" routerLink="/claim/{{claim.id}}/select-forms" *ngIf="claim.state == 'incomplete'">
          <div class="edit-icon"></div>Edit Claim
        </button>
        <button class="view" routerLink="/claim/{{claim.id}}" *ngIf="claim.state == 'submitted'">
          <div class="docs-icon"></div>View Claim
        </button>
      </div>
      <div class="claim-content">
        <div class="claim-header">
          <div class="icon-wrapper">
            <div class="icon"></div>
          </div>
          <div class="text-wrapper">
            <h4 class="status">
            <a class="claim-link"
               routerLink="/claim/{{claim.claimID}}/select-forms">
               {{claimConfig[claim.key].name}}</a>
            </h4>
            <div>{{createHeaderString(claim)}}</div>
            <div class="date">{{claim.stateUpdatedAt | date:'MM/dd/yyyy'}}</div>
          </div>
        </div>
        <div class="form-content" *ngIf="claimConfig[claim.key].forms > 0">
          <div class="form-icon-wrapper">
            <div class="docs-icon"></div>
          </div>
          <h4>Forms:</h4>
          <ul>
            <li *ngFor="let formId of claimConfig[claim.key]">{{formId}}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</div>
`;
@Component({
    selector: 'profile-claims',
    template: claimsTemplate
})
export class ProfileClaimsComponent implements OnInit {

    public user;
    public claims;
    public claimConfig: any;

    constructor(public activatedRoute: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.claimConfig = ClaimConfigService.getClaimConfig();
        this.claimConfig = keyBy(this.claimConfig.claims,
            (claimConfig) => claimConfig.key);
        this.user = this.activatedRoute.snapshot.data.user;
        this.claims = this.activatedRoute.snapshot.data.claims;
        console.log(this.claims);
        console.log(this.claimConfig);
    }

    createHeaderString(claim): String {
        if (claim.state == 'INCOMPLETE') {
            return 'Started (incomplete)';
        } else if (claim.state == 'SUBMITTED') {
            return 'Submitted';
        }
    }
}

const settingsTemplate = `
<div class="profile-section" id="settings">
  <div class="section">
    <div class="summary">Change your account password</div><a class="btn" href="/password/change">Change Password</a>
  </div>
  <div class="section">
    <div class="summary">Log out of your account</div><a class="btn logout" href="/signout">Log Out</a>
  </div>
  <div class="section">
    <div class="summary">Delete your account and all of your saved information</div>
    <button class="delete" (click)="clickDeleteAccount()">Delete Account</button>
  </div>
</div>
`;

@Component({
    selector: 'profile-settings',
    template: settingsTemplate,
    providers: [Location, {provide: LocationStrategy, useClass: PathLocationStrategy}]
})
export class ProfileSettingsComponent {

    constructor(public ngbModal: NgbModal,
                public router: Router,
                public ajaxService: AjaxService,
                @Inject(DOCUMENT) private document: any) {
    }

    clickDeleteAccount() {
        let modalRef = this.ngbModal.open(DeleteAccountModal);

        modalRef.result.then(
            (res) => {
                if (res == true) {
                    this.ajaxService.deleteUserAccount().subscribe(
                        (resp) => {
                            this.document.location.href = '/signout';
                        }
                    );
                }
            },
            () => {
                // Closed
            }
        )

    }
}


const profileTemplate = `
<div id="vfi-profile-view">
  <h3>My Profile</h3>
  <div class="profile-tabs">
    <button routerLink="/profile/general">General</button>
    <button routerLink="/profile/claims">Claims</button>
    <button routerLink="/profile/settings">Settings</button>
  </div>
  <div id="vfi-profile-container">
    <router-outlet></router-outlet>
  </div>
</div>
<app-footer></app-footer>
`;

@Component(
    {
        selector: "profile",
        template: profileTemplate,
        styleUrls: ['../assets/styles/profile.styl']
    }
)
export class ProfileComponent implements OnInit {

    public user;
    public claims;

    constructor(public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.user = this.activatedRoute.snapshot.data.user;
        this.claims = this.activatedRoute.snapshot.data.claims;
    }
}



