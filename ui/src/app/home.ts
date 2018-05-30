import {Component, OnInit} from '@angular/core';
import {ClaimService} from './claimService';
import {ActivatedRoute} from '@angular/router';


let template = `
<div id="vfi-home-view">
  <div class="banner-wrapper">
    <div class="banner-overlay">
      <div class="headline-wrapper row" *ngIf="incompleteClaim">
        <div class="col-sm-9">
          <h3 class="header">Helping U.S. veterans file their VA claims.</h3>
          <h5 class="subheader">Done smarter, easier, and safer.</h5>
          <h5 class="subheader">Continue your saved paperwork:</h5>
        </div>
        <div class="col-sm-3">
            <a routerLink="/claim/{{incompleteClaim.claimID}}/select-forms" class="btn">Continue</a>
        </div>
      </div>
      <div class="headline-wrapper row" *ngIf="!incompleteClaim">
        <div class="col-sm-9">
          <h3 class="header">Helping U.S. veterans file their VA claims.</h3>
          <h5 class="subheader">Done smarter, easier, and safer.</h5>
          <h5 class="subheader">Get the benefits that you deserve now.</h5>
        </div>
        <div class="col-sm-3">
            <a *ngIf="isSignedIn" routerLink="/claim/start" class="btn">Begin</a>
            <a *ngIf="!isSignedIn" href="/signup" class="btn">Begin</a>
        </div>
      </div>
    </div>
  </div>
  <div class="action-wrapper container">
    <div class="action row">
      <div class="content col-sm-6">
        <h4>Submit an Intent to File.</h4>
        <p>Secure an effective date for your claim today in 5 minutes. Get the largest possible retroactive payment if you are awarded benefits. Avoid eBenefits, avoid dealing with any paperwork.</p>
          <a routerLink="/claim/start" *ngIf="!incompleteClaim && isSignedIn" class="btn">
            Begin
          </a>
          <a routerLink="/claim/{{incompleteClaim.claimID}}/select-forms" *ngIf="incompleteClaim && isSignedIn" class="btn">
            Continue
          </a>
          <a href="/signup" *ngIf="!isSignedIn" class="btn">
            Begin
          </a>
      </div>
      <div class="icons-wrapper col-sm-6">
        <div class="main-wrapper folder right-align">
          <div class="main-icon folder"></div>
          <div class="inner-wrapper checkmark right-align">
            <div class="inner-icon checkmark"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="action-wrapper container">
    <div class="action row">
      <div class="content col-sm-6">
        <h4>See what the VA will pay you for your service connected disability.</h4>
        <p>The VA rules for disability compensation are complicated and change frequently. Use our simple tool to get the most up to date information on what you could be paid by the VA.</p>
          <a routerLink="/ratings" class="btn">
            Disability Ratings Calculator
          </a>
      </div>
      <div class="icons-wrapper col-sm-6">
        <div class="main-wrapper health right-align">
          <div class="main-icon health"></div>
          <div class="inner-wrapper question right-align">
            <div class="inner-icon question"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="action-wrapper container">
    <div class="action row">
      <div class="content col-sm-6">
        <h4>How does Vetafi work?</h4>
        <p>Have any questions about Vetafi and what services we provide? Want more information about the VA claims process? Visit our FAQ page to get your questions answered.</p>
          <a routerLink="/faq" class="btn">Frequently Asked Questions</a>
      </div>
      <div class="icons-wrapper col-sm-6">
        <div class="main-wrapper health right-align">
          <div class="main-icon health"></div>
          <div class="inner-wrapper question right-align">
            <div class="inner-icon question"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <app-footer></app-footer>
</div>
`;

@Component({
    selector: 'app-home',
    template: template,
    styleUrls: ['../assets/styles/home.styl']
})
export class HomeComponent implements OnInit {

    isSignedIn;
    currentClaim;
    user;
    claims;
    incompleteClaim;

    constructor(public claimService: ClaimService,
                private activatedRoute: ActivatedRoute) {
    }

    ngOnInit() {
        this.claims = this.activatedRoute.snapshot.data.claims || [];
        this.incompleteClaim = this.claims.find(
            (claim) =>
                claim.state === ClaimService.state.INCOMPLETE
        );
        this.isSignedIn = !!this.activatedRoute.snapshot.data.user;
        this.user = this.activatedRoute.snapshot.data.user;
    }
}
