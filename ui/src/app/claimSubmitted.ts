import {Component} from '@angular/core';

const template = `
<div id="claim-submitted-view">
  <app-breadcrumbs [page]="4"></app-breadcrumbs>
  <div id="claim-submitted-wrapper">
    <div class="header">
      <div class="header-icon-wrapper">
        <div class="header-icon"></div>
      </div>
      <h2>Submitted!</h2>
    </div>
    <div class="message success">
      <p>Congratulations!</p>
      <p>Your claim and all of its forms have been successfully sent to the VA.</p>
      <p>Expect your electronic copies to be sent to you within 5-10 minutes and<br/>look for your physical copies in the mail within 1-2 weeks.</p>
      <p>Thank you so much for using Vetafi! View your finished claim <a routerLink="/profile/claims">here</a>.</p>
    </div>
  </div>
</div>
<app-footer></app-footer>
`;
@Component({
    selector: "app-claim-submitted",
    template: template,
    styleUrls: ['../assets/styles/claimSubmitted.styl']
})
export class ClaimSubmittedComponent {


}
