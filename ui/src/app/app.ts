/**
 * Main AngularJS Web Application Declaration
 */
import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {FormlyModule} from '@ngx-formly/core';
import {FormlyBootstrapModule} from '@ngx-formly/bootstrap';
import {AjaxService} from './net';
import {ClaimService} from './claimService';
import {HomeComponent} from './home';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {CommonModule, DatePipe} from '@angular/common';
import {SignaturePadModule} from 'angular2-signaturepad';
import {CookieModule} from 'ngx-cookie';
import {FormConfigService} from './formConfigService';
import {ClaimConfigService} from './claimConfigService';
import {AutoCorrectField, FORMLY_CONFIG} from './formly';
import {DeleteAccountModal, ProfileClaimsComponent, ProfileComponent, ProfileGeneralComponent, ProfileSettingsComponent} from './profile';
import {ClaimStartComponent} from './claimStart';
import {ClaimSelectFormsComponent} from './claimSelectForms';
import {ClaimConfirmComponent} from './claimConfirm';
import {ClaimSubmittedComponent} from './claimSubmitted';
import {ClaimViewComponent} from './claimView';
import {FormComponent} from './form';
import {FaqComponent} from './faq';
import {SignDocumentComponent} from './signDocument';
import {SignDocumentPreviewComponent} from './signDocumentPreview';
import {HeaderComponent, SubscribeModalComponent} from './header';
import {BreadcrumbsComponent} from './breadcrumbs';
import {BusySpinnerComponent} from './downloadSpinner';
import {FooterComponent} from './footerDirective';
import {ChangePasswordModalComponent} from './modalChangePassword';
import {APP_ROUTES} from './app.routes';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {WindowRef} from './window';
import {RouterModule} from '@angular/router';
import {ClaimResolve, ClaimsResolve, FormsResolve, MaybeUserResolve, UserResolve, UserValuesResolve} from './resolvers';
import {RootComponent} from './root';
import {TosComponent, TosModalComponent} from './tos';
import {EditProfileComponent} from './modalEditProfile';


@NgModule({
    imports: [
        CommonModule,
        HttpClientModule,
        BrowserModule,
        FormlyBootstrapModule,
        FormlyModule.forRoot(FORMLY_CONFIG),
        RouterModule.forRoot(
            APP_ROUTES,
            {
                enableTracing: false,
                onSameUrlNavigation: 'reload',
                useHash: true
            }),
        NgbModule.forRoot(),
        SignaturePadModule,
        CookieModule.forRoot(),
        FormsModule,
        ReactiveFormsModule
    ],
    providers: [
        DatePipe,
        ClaimService,
        FormConfigService,
        ClaimConfigService,
        AjaxService,
        WindowRef,
        UserResolve,
        MaybeUserResolve,
        ClaimResolve,
        ClaimsResolve,
        UserValuesResolve,
        FormsResolve
    ],
    bootstrap: [
        RootComponent
    ],
    declarations: [
        RootComponent,
        HeaderComponent,
        BreadcrumbsComponent,
        ClaimConfirmComponent,
        ClaimSelectFormsComponent,
        ClaimStartComponent,
        ClaimSubmittedComponent,
        ClaimViewComponent,
        BusySpinnerComponent,
        FaqComponent,
        FooterComponent,
        FormComponent,
        TosComponent,
        HomeComponent,
        ChangePasswordModalComponent,
        SubscribeModalComponent,
        TosModalComponent,
        EditProfileComponent,
        DeleteAccountModal,
        SignDocumentComponent,
        SignDocumentPreviewComponent,
        ProfileClaimsComponent,
        ProfileComponent,
        ProfileGeneralComponent,
        ProfileSettingsComponent,
        AutoCorrectField
    ],
    entryComponents: [
        TosModalComponent,
        AutoCorrectField,
        DeleteAccountModal
    ]
})
export class AppModule {
}
