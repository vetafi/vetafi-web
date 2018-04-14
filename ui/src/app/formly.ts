import {FormControl} from '@angular/forms';
import {ConfigOption, FieldType, FormlyFieldConfig} from '@ngx-formly/core';
import {Component, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import parse from 'date-fns/parse'


export function PhoneNumberValidator(c: FormControl) {
    return /\d{3}-\d{3}-\d{4}/.test(c.value) ? null : { 'phoneNumber': true };
}

export function PhoneNumberValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid phone number`;
}

export function SSNValidator(c: FormControl) {
    return /\d{3}-\d{2}-\d{4}/.test(c.value) ? null : { 'ssn': true };
}

export function SSNValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid ssn`;
}

export function CountryValidator(c: FormControl) {
    return /^[A-Z]{2}$/.test(c.value) ? null : { 'country': true };
}

export function CountryValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid country code`;
}

export function StateValidator(c: FormControl) {
    return /^[A-Z]{2}$/.test(c.value) ? null : { 'state': true };
}

export function StateValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid US state code`;
}

export function EmailValidator(c: FormControl) {
    return /.+@.+\..+/.test(c.value) ? null : { 'email': true };
}

export function EmailValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid email`;
}

export function ZipcodeValidator(c: FormControl) {
    return /\d{5}/.test(c.value) ? null : { 'zipCode': true };
}

export function ZipcodeValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid zipcode`;
}

export function DateValidator(c: FormControl) {
    let parsed = parse(c.value);
    return parsed.toDateString() !== 'Invalid Date' ? null : {'date': true};
}

export function DateValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid date`;
}

export function MiddleInitialValidator(c: FormControl) {
    return /^[A-Z]{1}$/.test(c.value) ? null : { 'middleInitial': true };
}

export function MiddleInitialValidatorMessage(err, field: FormlyFieldConfig) {
    return `"${field.formControl.value}" is not a valid middle initial, should be 1 letter`;
}

@Component({
    selector: 'formly-field-input',
    template: `
        <input [type]="type"
               [formControl]="formControl"
               class="form-control"
               [formlyAttributes]="field"
               [attr.autocomplete]="autocomplete"
               [class.is-invalid]="showError"
               (input)="ngOnChanges($event)">
    `,
    host: {
        '[class.d-inline-flex]': 'to.addonLeft || to.addonRight',
    },
})
export class AutoCorrectField extends FieldType implements OnInit, OnChanges {

    autocomplete: String;

    get type() {
        return this.to.type || 'text';
    }

    ngOnInit(): void {
        super.ngOnInit();
        this.autocomplete = this.to['autocomplete'];
    }

    ngOnChanges(changes: SimpleChanges): void {
        super.ngOnChanges(changes);
        this.formControl.setValue(this.model[this.key]);
    }
}

export function phoneNumberParser(value, index) {
    if (!value) {
        return '';
    }
    value = value.replace(new RegExp('[^0-9]', 'g'), '');
    let first = value.substr(0, 3);
    let second = value.substr(3, 3);
    let third = value.substr(6, 4);

    if (first.length <= 3 && second.length == 0) {
        return first;
    }

    if (first.length == 3 && second.length > 0 &&
        second.length <= 3 && third.length == 0) {
        return first + '-' + second;
    }

    if (second.length == 3 && third.length > 0) {
        return first + '-' + second + '-' + third;
    }
}

export function ssnParser(value, index) {
    if (!value) {
        return '';
    }
    value = value.replace(new RegExp('[^0-9]', 'g'), '');
    let first = value.substr(0, 3);
    let second = value.substr(3, 2);
    let third = value.substr(5, 4);

    if (first.length <= 3 && second.length == 0) {
        return first;
    }

    if (first.length == 3 && second.length == 0) {
        return first + '-';
    }

    if (first.length == 3 && second.length > 0 &&
        second.length <= 2 && third.length == 0) {
        return first + '-' + second;
    }

    if (second.length == 2 && third.length > 0) {
        return first + '-' + second + '-' + third;
    }
}

export function zipCodeParser(value, index) {
    if (!value) {
        return '';
    }
    return value.replace(new RegExp('[^0-9]', 'g'), '').substring(0, 5);
}

export function twoLetterCodeParser(value, index) {
    if (!value) {
        return '';
    }
    return value.toUpperCase().replace(new RegExp('[^A-Z]', 'g'), '').substring(0, 2);
}

export function oneLetterCodeParser(value, index) {
    if (!value) {
        return '';
    }
    return value.toUpperCase().replace(new RegExp('[^A-Z]', 'g'), '').substring(0, 1);
}

export function dateParser(value, index) {
    if (!value) {
        return '';
    }

    value = value.replace(new RegExp('[^0-9]', 'g'), '');
    let first = value.substr(0, 2);
    let second = value.substr(2, 2);
    let third = value.substr(4, 4);

    if (first.length <= 2 && second.length == 0) {
        return first;
    }

    if (first.length == 2 && second.length > 0 &&
        second.length <= 2 && third.length == 0) {
        return first + '/' + second;
    }

    if (second.length == 2 && third.length > 0) {
        return first + '/' + second + '/' + third;
    }
}

export const FORMLY_CONFIG: ConfigOption =
    {
        validators: [
            {
                name: 'zipCode',
                validation: ZipcodeValidator
            },
            {
                name: 'phoneNumber',
                validation: PhoneNumberValidator
            },
            {
                name: 'ssn',
                validation: SSNValidator
            },
            {
                name: 'state',
                validation: StateValidator
            },
            {
                name: 'country',
                validation: CountryValidator
            },
            {
                name: 'email',
                validation: EmailValidator
            },
            {
                name: 'date',
                validation: DateValidator
            },
            {
                name: 'middleInitial',
                validation: MiddleInitialValidator
            }
        ],
        validationMessages: [
            {
                name: 'zipCode',
                message: ZipcodeValidatorMessage
            },
            {
                name: 'phoneNumber',
                message: PhoneNumberValidatorMessage
            },
            {
                name: 'ssn',
                message: SSNValidatorMessage
            },
            {
                name: 'state',
                message: StateValidatorMessage
            },
            {
                name: 'country',
                message: CountryValidatorMessage
            },
            {
                name: 'email',
                message: EmailValidatorMessage
            },
            {
                name: 'date',
                message: DateValidatorMessage
            },
            {
                name: 'middleInitial',
                message: MiddleInitialValidatorMessage
            }
        ],
        types: [
            {
                name: 'input',
                component: AutoCorrectField
            },
            {
                name: 'phoneNumber',
                defaultOptions: {
                    templateOptions: {
                        placeholder: '000-000-0000'
                    },
                    validators: {
                        validation: [PhoneNumberValidator]
                    },
                    parsers: [
                        phoneNumberParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'ssn',
                defaultOptions: {
                    templateOptions: {
                        placeholder: '000-00-0000'
                    },
                    validators: {
                        validation: ['ssn']
                    },
                    parsers: [
                        ssnParser
                    ],
                    component: AutoCorrectField,
                    validation: {
                        show: true
                    }
                },
                component: AutoCorrectField
            },
            {
                name: 'zipCode',
                defaultOptions: {
                    templateOptions: {
                        placeholder: '00000'
                    },
                    validators: {
                        validation: [ZipcodeValidator]
                    },
                    parsers: [
                        zipCodeParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'state',
                defaultOptions: {
                    templateOptions: {
                        placeholder: 'XX'
                    },
                    validators: {
                        validation: [StateValidator]
                    },
                    parsers: [
                        twoLetterCodeParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'country',
                defaultOptions: {
                    templateOptions: {
                        placeholder: 'XX'
                    },
                    validators: {
                        validation: [CountryValidator]
                    },
                    parsers: [
                        twoLetterCodeParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'middleInitial',
                defaultOptions: {
                    templateOptions: {
                        placeholder: 'X'
                    },
                    validators: {
                        validation: [MiddleInitialValidator]
                    },
                    parsers: [
                        oneLetterCodeParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'date',
                defaultOptions: {
                    templateOptions: {
                        placeholder: 'MM/DD/YYYY'
                    },
                    validators: {
                        validation: [DateValidator]
                    },
                    parsers: [
                        dateParser
                    ],
                    component: AutoCorrectField
                }
            },
            {
                name: 'email',
                defaultOptions: {
                    templateOptions: {
                        placeholder: 'yourname@website.com'
                    },
                    validators: {
                        validation: [EmailValidator]
                    }
                }
            }
        ]
    };
