import {Injectable} from '@angular/core';
import * as claimConfig from '../../../conf/claims.json'

@Injectable()
export class ClaimConfigService {

    static getClaimConfig() {
        return claimConfig;
    }
}
