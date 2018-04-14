import {Component, Input} from '@angular/core';


const template = `
<div id="vfi-footer-view">
  <div class="footer-wrapper container">
    <div class="footer-layout links row">
      <div class="footer-section col-sm-6">
        <a routerLink="/home">Home</a>
        <a routerLink="/faq">FAQ</a>
      </div>
      <div class="footer-section col-sm-6">
        <a routerLink="/profile/general" *ngIf="isSignedIn">My Profile</a>
        <a routerLink="/tos">Terms of Service</a>
        <a href="mailto:info@vetafi.org">Support</a>
      </div>
    </div>
    <div class="footer-layout disclaimer row">
      <div class="footer-section col-sm-2"><img src="assets/icons/vetafi-icon.svg"/></div>
      <div class="footer-section col-sm-10">
        <p>Vetafi is an independent non-profit organization. It is not affiliated with the U.S. Department of Veteran Affairs. We do not sell advertising and will not market to you. Our only goal is to help our returning military veterans file their claims with the VA.</p>
      </div>
    </div>
  </div>
</div>
`;


@Component({
    selector: "app-footer",
    template: template,
    styleUrls: ['../assets/styles/footer.styl']
})
export class FooterComponent {

    @Input("user")
    public user;

    isSignedIn(): boolean {
        return this.user != null;
    }
}
