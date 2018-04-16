import {Component, DoCheck, HostListener, KeyValueDiffer, KeyValueDiffers, OnInit} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import keyBy from 'lodash/keyBy';
import clone from 'lodash/clone';
import sumBy from 'lodash/sumBy';
import isEqual from 'lodash/isEqual';
import {WindowRef} from './window';
import {FormConfigService} from './formConfigService';
import {AjaxService} from './net';
import {ActivatedRoute, Router} from '@angular/router';
import {FormGroup} from '@angular/forms';

/**
 * Controller for the form filling page.
 */
const template = `
<div id="vfi-form-view">
  <h2 class="form-title">{{title}}</h2>
  <p>{{description}}</p>
  <hr/>
  <form (ngSubmit)="onSubmit()" name="form" autocomplete="on">
    <formly-form [form]="form" [model]="model" [fields]="fields"></formly-form>
    <div class="container-fluid nopadding">
      <div class="row form-submit-button-group">
        <div class="col-xs-12">
          <button class="submit" type="submit" [disabled]="answered != answerable || !form.valid ? 'disabled' : false">Submit</button>
          <button class="save" type="button" (click)="onSave()">Save Information</button>
          <button class="download" type="button" (click)="onDownload()">Download</button>
        </div>
      </div>
    </div>
  </form>
</div>
<div class="floating-progress" *ngIf="answerable > 0">
    <div class="col-xs-3 floating-progress-text">
        <span class="progress-numbers">{{answered}} / {{answerable}}</span>
    </div>
  <ngb-progressbar [animated]="false" [value]="getProgress()"></ngb-progressbar>
    
</div>
`;

@Component({
    selector: 'app-form',
    template: template,
    styleUrls: ['../assets/styles/form.styl']
})
export class FormComponent implements OnInit, DoCheck {

    fieldsByKey;
    model;
    form = new FormGroup({});
    busy: boolean;
    fields;
    fieldsCopy;
    answered: number;
    answerable: number;
    claimId;
    formId;
    title;
    description;
    modelDiffer: KeyValueDiffer<string, any>;
    private lastModel;

    constructor(public windowRef: WindowRef,
                public formConfigService: FormConfigService,
                public ajaxService: AjaxService,
                public router: Router,
                public activatedRoute: ActivatedRoute,
                private differs: KeyValueDiffers) {

    }

    ngOnInit(): void {
        this.claimId = this.activatedRoute.snapshot.params.claimId;
        this.formId = this.activatedRoute.snapshot.params.formId;
        this.model = this.activatedRoute.snapshot.data.userValues.values;
        this.title = this.formConfigService.getFormConfig()[this.formId].vfi.title;
        this.description = this.formConfigService.getFormConfig()[this.formId].vfi.description;
        this.fields = this.formConfigService.getFormConfig()[this.formId].fields;
        this.fieldsCopy = this.formConfigService.getFormConfig()[this.formId].fields;
        this.fieldsByKey = keyBy(this.fieldsCopy, (f) => f.key);
        this.modelDiffer = this.differs.find(this.model).create();
        this.updateProgress();
    }


    ngDoCheck(): void {
        const changes = this.modelDiffer.diff(this.model);
        if (changes) {
            this.updateProgress();
        }
    }

    onDownload() {
        let popUp = this.windowRef.nativeWindow.open('/loading', '_blank');
    }

    onSubmit() {
        this.busy = true;
        this.saveForm(true).subscribe(
            (res) => {
                this.router.navigateByUrl(
                    'claim/' + this.activatedRoute.snapshot.params.claimId + '/select-forms');
            }
        );
    }

    onSave() {
        this.busy = true;
        this.saveForm(true).subscribe(
            (res) =>
                this.busy = false
        );
    }

    saveForm(force: boolean): Observable<any> {
        if (this.lastModel == null || !isEqual(this.lastModel, this.model) || force === true) {
            this.lastModel = clone(this.model);
            return this.ajaxService.saveForm(this.activatedRoute.snapshot.params.claimId,
                this.activatedRoute.snapshot.params.formId,
                this.model);
        } else {
            return null; //todo
        }
    }

    countAnswerable(model): number {
        return Object.keys(this.form.controls).length -
            sumBy(this.fieldsCopy,
                (field: any) => field.templateOptions.optional ? 1 : 0);
    }

    countAnswered(model) {
        let k, count = 0;
        for (k in this.fieldsByKey) {
            if (this.fieldsByKey.hasOwnProperty(k)) {
                if (model.hasOwnProperty(k) &&
                    model[k] !== '' &&
                    !this.fieldsByKey[k].templateOptions.optional &&
                    this.form.controls.hasOwnProperty(k) &&
                    this.form.controls[k].status == "VALID") {
                    count++;
                }
            }
        }

        return count;
    }

    getProgress() {
        return (this.answered / this.answerable) * 100.0;
    }

    updateProgress() {
        this.answered = this.countAnswered(this.model);
        this.answerable = this.countAnswerable(this.model);
    }

    @HostListener('window:visibilitychange')
    onVisibilityChange() {
        this.saveForm(false);
    }

    @HostListener('window:onbeforeunload')
    onBeforeUnload() {
        this.saveForm(false);
    }
}
