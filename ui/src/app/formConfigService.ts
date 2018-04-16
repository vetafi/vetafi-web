import {Injectable} from '@angular/core';
import * as vba210966are from '../../../conf/forms/formly_configs/VBA-21-0966-ARE.json'
import cloneDeep from 'lodash/cloneDeep';

@Injectable()
export class FormConfigService {

    getFormConfig() {
        return cloneDeep({
            'VBA-21-0966-ARE': vba210966are
        });
    }
}
