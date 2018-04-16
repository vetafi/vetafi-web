import {Component, Input, OnInit} from '@angular/core';
import {AjaxService} from "./net";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import cloneDeep from 'lodash/cloneDeep'

const template = `

<div class="vfi-modal">
  <div class="close-wrapper">
    <button class="close-icon" (click)="activeModal.dismiss()"></button>
  </div>
  <div class="header">
    <h3>Edit Profile</h3>
  </div>
  <div id="vfi-modal-edit-profile">
    <form class="form-horizontal">
      <div class="form-section">
        <h4>Your Name</h4>
        <div class="form-group has-feedback" [ngClass]="nameGroup.feedbackFn()">
          <div *ngFor="let field of nameGroup.fields">
            <label class="control-label col-sm-3" for="{{field.ngModel}}">{{field.label}}</label>
            <div class="col-sm-9">
              <input class="form-control" [attr.id]="field.ngModel" [(ngModel)]="userDraft[field.ngModel]" placeholder="{{field.placeholder}}"/>
            </div>
          </div>
        </div>
      </div>
      <div class="form-section">
        <h4>Email</h4>
        <div class="form-group has-feedback" [ngClass]="emailGroup.feedbackFn()">
          <div *ngFor="let field of emailGroup.fields">
            <label class="control-label col-sm-3" for="{{field.ngModel}}">{{field.label}}</label>
            <div class="col-sm-9">
              <input class="form-control" [attr.id]="field.ngModel" [(ngModel)]="userDraft[field.ngModel]" placeholder="{{field.placeholder}}"/>
            </div>
          </div>
        </div>
      </div>
      <div class="form-section">
        <h4>Primary Address</h4>
        <div class="form-group has-feedback" *ngFor="let field of addressGroup" [ngClass]="validateAddressField(field)">
          <label class="control-label col-sm-3" for="{{field.ngModel}}">{{field.label}}</label>
          <div class="col-sm-9">
            <input class="form-control" [attr.id]="field.ngModel" [(ngModel)]="userDraft.contact.address[field.ngModel]" placeholder="{{field.placeholder}}"/>
          </div>
        </div>
      </div>
    </form>
    <div class="button-wrapper">
      <button class="cancel" (click)="activeModal.dismiss()">Cancel</button>
      <button class="save" [ngClass]="allFilled() ? '' : 'faded'" (click)="saveInfo()">Save</button>
      <div class="clearer"></div>
    </div>
  </div>
</div>
`;


@Component({
    selector: 'app-edit-profile',
    template: template,
    styleUrls: ['../assets/styles/modals/editProfile.styl']
})
export class EditProfileComponent implements OnInit {


    @Input('user') user;
    newUser;
    nameGroup;
    emailGroup;
    addressGroup;

    constructor(public ajaxService: AjaxService,
                public activeModal: NgbActiveModal) {
    }

    ngOnInit(): void {
        this.newUser = cloneDeep(this.user);
        this.nameGroup = {
            feedbackFn: function() { return this.newUser.firstname && this.newUser.lastname ? 'has-success' : 'has-error'; },
            fields: [
                {
                    label: 'First name',
                    ngModel: 'firstname',
                    placeholder: 'Enter your first name'
                },
                {
                    label: 'Middle name',
                    ngModel: 'middlename',
                    placeholder: 'Enter your middle name (if any)'
                },
                {
                    label: 'Last name',
                    ngModel: 'lastname',
                    placeholder: 'Enter your last name'
                }
            ]
        };
        this.emailGroup = {
            feedbackFn: function() { return this.validateEmails() ? 'has-success' : 'has-error'; },
            fields: [
                {
                    label: 'Primary email',
                    ngModel: 'email',
                    placeholder: 'Enter your email address'
                },
                {
                    label: 'Confirm email',
                    ngModel: 'confirmEmail',
                    placeholder: 'Re-enter your email address (if changing email)'
                }
            ]
        };
        this.addressGroup = [
            {
                label: 'Address Name',
                ngModel: 'name',
                placeholder: 'Name of address i.e. Home, Work, etc.'
            },
            {
                label: 'Street address',
                ngModel: 'street1',
                placeholder: 'i.e. 1234 Washington St'
            },
            {
                label: '',
                ngModel: 'street2',
                placeholder: 'i.e. Room, Apartment, Suite No. (if any)'
            },
            {
                label: 'Country',
                ngModel: 'country',
                placeholder: 'Country of residence'
            },
            {
                label: 'City',
                ngModel: 'city',
                placeholder: 'City of residence'
            },
            {
                label: 'State / Province',
                ngModel: 'province',
                placeholder: 'State or province'
            },
            {
                label: 'Zip / Postal',
                ngModel: 'postal',
                placeholder: 'Zip or Postal code'
            },
        ];


    }

    static validateEmail(email: String): boolean {
        if (!email) {
            return false;
        }
        let atInd = email.indexOf('@');
        let dotInd = email.indexOf('.');
        return atInd > -1 && dotInd > atInd;
    }

    validateEmails(newUser) {
        return newUser.email == this.user.email ||
            (
                EditProfileComponent.validateEmail(newUser.email)
                && EditProfileComponent.validateEmail(newUser.confirmEmail)
                && newUser.email == newUser.confirmEmail
            );
    }

    allFilled(): boolean {
        return this.newUser.firstname && this.newUser.lastname
            && this.validateEmails(this.newUser)
            && this.newUser.contact.address.name
            && this.newUser.contact.address.street1
            && this.newUser.contact.address.country
            && this.newUser.contact.address.city
            && this.newUser.contact.address.province
            && this.newUser.contact.address.postal;
    }

    saveInfo() {
        if (!this.allFilled()) {
            return
        }

        this.ajaxService.editUserInfo(this.newUser).subscribe(
            (response) => {
                this.activeModal.close(response);
            },
            (err) => {
                console.log(err);
            }
        )
    }

    validateAddressField = function(field) {
        if (field.ngModel == 'street2') { // optional fields
            return '';
        }
        return this.newUser.contact.address[field.ngModel] ? 'has-success' : 'has-error';
    }
}
