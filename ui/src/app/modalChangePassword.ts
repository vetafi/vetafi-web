import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {AjaxService} from "./net";

const template = `
<div class="vfi-modal">
  <div class="close-wrapper">
    <button class="close-icon" (click)="activeModal.dismiss()"></button>
  </div>
  <div class="header">
    <h3>Change Password</h3>
  </div>
  <div id="vfi-modal-change-password" modal-change-password="modal-change-password">
    <form>
      <h4>Old Password</h4>
      <input class="old" [(ngModel)]="oldPwd" placeholder="Enter your current password" type="password"/>
      <h4>New Password</h4>
      <input class="new" [(ngModel)]="newPwd" placeholder="Enter your new password" type="password"/>
      <h4>Confirm Password</h4>
      <input class="confirm" [(ngModel)]="confirmPwd" placeholder="Re-enter your new password" type="password"/>
    </form>
    <div class="error-msg">{{errorMsg}}</div>
    <div class="button-wrapper">
      <button class="cancel" (click)="activeModal.dismiss()">Cancel</button>
      <button class="save" (click)="savePassword()">Save</button>
      <div class="clearer"></div>
    </div>
  </div>
</div>
`;

@Component({
    selector: 'app-change-password-modal',
    template: template,
    styleUrls: ['../assets/styles/modals/changePassword.styl']
})
export class ChangePasswordModalComponent {
    errorMsg;
    oldPwd;
    newPwd;
    confirmPwd;

    constructor(public activeModal: NgbActiveModal,
                public ajaxService: AjaxService) {
        this.activeModal = activeModal;
        this.ajaxService = ajaxService;
    }

    displayError(error) {
        this.errorMsg = error;
    }

    savePassword() {
        if (!this.oldPwd) {
            this.displayError("Please enter your old password.");
            return;
        }

        if (!this.newPwd) {
            this.displayError("Please enter a new password.");
            return;
        }

        if (this.newPwd && this.newPwd.length < 6) {
            this.displayError("Your new password is too short.");
            return;
        }

        if (this.confirmPwd != this.newPwd) {
            this.displayError("Your new password and confirm password do not match! Please re-type your new password.");
            return;
        }

        if (this.newPwd == this.oldPwd) {
            this.displayError("Your new password cannot match your old password.");
            return;
        }

        this.ajaxService.changePassword(this.oldPwd, this.newPwd).subscribe(
            (res) => {
                this.activeModal.close()
            },
            (error) => {
                if ('auth_mismatch' == error.data) {
                    this.displayError("Incorrect current password.");
                } else {
                    this.displayError("Sorry, unknown server issue. Please try again later.");
                }
            }
        );
    }
}
