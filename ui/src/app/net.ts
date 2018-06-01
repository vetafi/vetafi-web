import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from 'rxjs/Observable';
import {TestMessage} from './models/test_pb';

@Injectable()
export class AjaxService {

    constructor(private httpClient: HttpClient) {
    }

    getAuthIdMeUrl(): String {
        return '/auth/idme';
    }

    touchSession(): Observable<any> {
        return this.httpClient
            .get('/api/session/touch')
    }

    changePassword(oldPwd: String, newPwd: String): Observable<any> {
        return this.httpClient
            .post('/auth/password',
                {oldPwd: oldPwd, newPwd: newPwd})
    }

    getUserInfo(): Observable<any> {
        return this.httpClient.get('/api/user').pipe(
            x => {
                console.log("got x:" + x);
                return x;
            }
        )
    }

    getUserValues(): Observable<any> {
        return this.httpClient.get('/api/user/values')
    }

    editUserInfo(data): Observable<any> {
        return this.httpClient.get('/api/user', data)
    }

    deleteUserAccount(): Observable<any> {
        return this.httpClient.delete('/api/user')
    }

    getClaimsForUser(): Observable<any> {
        return this.httpClient.get('/api/claims')
    }

    startClaim(data): Observable<any> {
        return this.httpClient.post('/api/claims/create', data)
    }

    submitClaim(claimId, data): Observable<any>  {
        return this.httpClient.post('/api/claim/submit/' + claimId, data)
    }

    discardClaim(claimId): Observable<any>  {
        return this.httpClient.delete('/api/claim/' + claimId)
    }

    getClaim(claimId): Observable<any>  {
        return this.httpClient.get('/api/claim/' + claimId)
    }

    getFormsForClaim(claimId): Observable<any>  {
        return this.httpClient.get('/api/forms/' + claimId)
    }

    saveForm(claimId, formId, data): Observable<any>  {
        return this.httpClient.post('/api/save/' + claimId + '/' + formId, data);
    }

    downloadForm(claimId, formId): Observable<any>  {
        return this.httpClient.get('/api/claim/' + claimId + '/form/' + formId + '/pdf');
    }

    getFormSignatureStatus(claimId, formId): Observable<any>  {
        return this.httpClient.get('/api/form/' + claimId + '/' + formId + '/issigned');
    }

    signClaim(claimId): Observable<any>  {
        return this.httpClient.get('/api/claim/sign/' + claimId)
    }

    subscribe(data): Observable<any>  {
        return this.httpClient.post('/api/subscribe', data)
    }

    agreeToTOS(): Observable<any> {
        return this.httpClient.get('/api/user/agreeToTOS')
    }

    getRatingsConfig(): Observable<any> {
        return this.httpClient.get('/api/ratings')
    }

    test(): Observable<TestMessage.AsObject> {
        return this.httpClient.get<TestMessage.AsObject>('/api/test')
    }
}
