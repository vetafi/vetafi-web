import {Component, OnInit} from '@angular/core';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ClaimService} from './claimService';
import {AjaxService} from './net';
import {ActivatedRoute} from '@angular/router';


let subscribeTemplate = `
<div class="vfi-modal">
  <div class="close-wrapper">
    <button class="close-icon" (click)="activeModal.dismiss()"></button>
  </div>
  <div class="header">
    <h3>{{headline}}</h3>
  </div>
  <div id="basic-two-button-modal">
    <div class="message">
      <p>{{message}}</p>
    </div>
    <form>
      <input [(ngModel)]="email" placeholder="Email Address" type="text" name="email"/>
    </form>
    <div class="error-msg">{{errorMsg}}</div>
    <div class="button-wrapper">
      <button class="cancel" (click)="activeModal.dismiss()">Cancel</button>
      <button class="save" (click)="submitSubscribe(email)">Save</button>
      <div class="clearer"></div>
    </div>
  </div>
</div>
`;


@Component({
    template: subscribeTemplate,
    styleUrls: ['../assets/styles/header.styl']
})
export class SubscribeModalComponent {
    headline: String;
    message: String;
    errorMsg: String;
    email: String;

    constructor(public activeModal: NgbActiveModal,
                public ajaxService: AjaxService) {
    }

    submitSubscribe(email: String) {
        this.ajaxService.subscribe({email: email, subscriptionType: 'INTERESTED_IN_UPDATES'}).subscribe(
            (res) => {
                this.activeModal.dismiss();
            },
            (err) => {
                this.errorMsg = 'There was an error subscribing your email.';
            }
        );
    }
}


let template = `
<div id="header">
  <div id="header-content"><a class="vfi-header-logo-wrapper" routerLink="/home"><img src="assets/icons/vetafi-logo.svg"/></a>
    <div class="header-links">
      <div *ngIf="!isSignedIn">
        <a class="big-link" href="/signup">
            <h5>Sign Up</h5>
            <div class="underline-border"></div>
        </a>  
        <a class="big-link" href="/signin">
            <h5>Log In</h5>
            <div class="underline-border"></div>
        </a>
      </div>
      <div *ngIf="isSignedIn">
          <a class="big-link" routerLink="/profile/general">
            <h5>My Profile</h5>
            <div class="underline-border"></div>
          </a>  
          <a class="big-link" href="/signout">
            <h5>Log Out</h5>
            <div class="underline-border"></div>
          </a>
      </div>
    </div>
  </div>
</div>
<div class="main-view-wrapper">
  <router-outlet></router-outlet>
</div>
<app-busy-spinner></app-busy-spinner>
`;

@Component({
    selector: 'header',
    template: template,
    styleUrls: ['../assets/styles/header.styl']
})
export class HeaderComponent implements OnInit {
    isSignedIn;
    hasIncompleteClaim;
    currentClaim;
    showMenu: boolean = false;
    user;

    constructor(public claimService: ClaimService,
                public ngbModal: NgbModal,
                private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.hasIncompleteClaim = this.claimService.hasIncompleteClaim();
        this.currentClaim = this.claimService.currentClaim || {};
        this.user = this.route.snapshot.data.user;
        console.log(this.user);
        this.isSignedIn = this.user != null;
    }

    clickSubscribe() {
        const modalRef = this.ngbModal.open(SubscribeModalComponent);
        modalRef.componentInstance.headline = 'Subscribe for Updates';
    }

    onToggleMenu() {
        this.showMenu = true;
        console.log(this);
    }

    closeThisMenu() {
        this.showMenu = false;
    }
}
