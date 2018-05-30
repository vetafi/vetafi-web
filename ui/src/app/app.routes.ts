import {HeaderComponent} from './header';
import {HomeComponent} from './home';
import {ProfileClaimsComponent, ProfileComponent, ProfileGeneralComponent, ProfileSettingsComponent} from './profile';
import {ClaimStartComponent} from './claimStart';
import {ClaimSelectFormsComponent} from './claimSelectForms';
import {ClaimConfirmComponent} from './claimConfirm';
import {ClaimSubmittedComponent} from './claimSubmitted';
import {ClaimViewComponent} from './claimView';
import {FormComponent} from './form';
import {FaqComponent} from './faq';
import {SignDocumentComponent} from './signDocument';
import {SignDocumentPreviewComponent} from './signDocumentPreview';
import {Routes} from '@angular/router';
import {ClaimsResolve, FormsResolve, MaybeUserResolve, RatingsConfigResolve, UserResolve, UserValuesResolve} from './resolvers';
import {TosComponent} from './tos';
import {RatingsCategories, RatingsHome, RatingsSelect} from './ratings';


let profileRoutes: Routes = [
    {
        path: 'general',
        component: ProfileGeneralComponent,
        resolve: {
            user: UserResolve,
            claims: ClaimsResolve
        }
    },
    {
        path: 'claims',
        component: ProfileClaimsComponent,
        resolve: {
            user: UserResolve,
            claims: ClaimsResolve
        }
    },
    {
        path: 'settings',
        component: ProfileSettingsComponent,
        resolve: {
            user: UserResolve,
            claims: ClaimsResolve
        }
    }
];

let childRoutes: Routes = [
    {
        path: '',
        redirectTo: 'home',
        pathMatch: 'full'
    },
    {
        path: 'home',
        component: HomeComponent,
        resolve: {
            user: MaybeUserResolve,
            claims: ClaimsResolve
        }
    },
    {
        path: 'profile',
        component: ProfileComponent,
        children: profileRoutes,
        resolve: {
            user: UserResolve,
            claims: ClaimsResolve
        }
    },
    {
        path: 'claim/start',
        component: ClaimStartComponent,
        resolve: {
            user: MaybeUserResolve,
            userClaims: ClaimsResolve
        }
    },
    {
        path: 'claim/:claimId/select-forms',
        component: ClaimSelectFormsComponent,
        resolve: {
            claimForms: FormsResolve,
            user: UserResolve
        }
    },
    {
        path: 'claim/:claimId/confirm',
        component: ClaimConfirmComponent,
        resolve: {
            claimForms: FormsResolve,
            user: UserResolve
        }
    },
    {
        path: 'claim/:claimId/submit',
        component: ClaimSubmittedComponent,
        resolve: {
            user: UserResolve
        }
    },
    {
        path: 'claim/:claimId',
        component: ClaimViewComponent,
        resolve: {
            user: UserResolve
        }
    },
    {
        path: 'claim/:claimId/form/:formId',
        component: FormComponent,
        resolve: {
            userValues: UserValuesResolve,
            user: UserResolve
        }
    },
    {
        path: 'faq',
        component: FaqComponent
    },
    {
        path: 'tos',
        component: TosComponent
    },
    {
        path: 'sign/:claimId',
        component: SignDocumentComponent,
        resolve: {
            claimForms: FormsResolve,
            user: UserResolve
        }
    },
    {
        path: 'sign/:claimId/:formId',
        component: SignDocumentPreviewComponent,
        resolve: {
            userValues: UserValuesResolve,
            user: UserResolve
        }
    },
    {
        path: 'ratings',
        component: RatingsHome,
        resolve: {
            ratingsConfig: RatingsConfigResolve
        }
    },
    {
        path: 'ratings/category/:categoryPath',
        component: RatingsCategories,
        resolve: {
            ratingsConfig: RatingsConfigResolve
        }
    },
    {
        path: 'ratings/category',
        component: RatingsCategories,
        resolve: {
            ratingsConfig: RatingsConfigResolve
        }
    },
    {
        path: 'ratings/category/:categoryPath/rating/:ratingPath',
        component: RatingsSelect,
        resolve: {
            ratingsConfig: RatingsConfigResolve
        }
    }
];

export const APP_ROUTES: Routes = [
    {
        path: '',
        component: HeaderComponent,
        resolve: {
            user: MaybeUserResolve,
            userClaims: ClaimsResolve
        },
        children: childRoutes
    }
];
