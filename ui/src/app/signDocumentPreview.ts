import {Component, ElementRef, HostListener, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {DatePipe} from "@angular/common";
import {AjaxService} from "./net";
import {ActivatedRoute, Router} from '@angular/router';
import {SignaturePad} from 'angular2-signaturepad/signature-pad';


const template = `
<div id="vfi-sign-document-preview-view">
  <app-breadcrumbs page="2"></app-breadcrumbs>
  <div id="sign-document-preview-wrapper">
    <h2 class="header">Sign Document</h2>
    <div class="section">
      <div class="container-fluid nopadding">
        <div class="row" *ngFor="let page of pages">
            <img class="pdf-page" [src]="'/pdfpreview/' + claimId + '/' + formId + '/' + page"/>
        </div>
        
        <div class="row">
          <p>Sign Here:</p>
          <!--   dataurl="signature" clear="clear"-->
          <div id="signature-container" #signatureContainer>
            <signature-pad></signature-pad>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-2">
            <button class="clear-signature" type="button" (click)="clear()">Clear Signature</button>
          </div>
          <div class="col-xs-2">
            <button (click)="onSubmit()">Sign</button>
          </div>
        </div>
        
      </div>
    </div>
  </div>
</div>
`;

@Component({
    selector: 'app-claim-confirm',
    template: template,
    styleUrls: ['../assets/styles/signDocumentPreview.styl']
})
export class SignDocumentPreviewComponent implements OnInit {

    userValues;
    pages: Number[] = [0];
    claimId: String;
    formId: String;
    signature: String;
    @ViewChild(SignaturePad) signaturePad: SignaturePad;
    @ViewChildren('signatureContainer') public signatureContainerQuery: QueryList<ElementRef>;
    public signatureContainer: ElementRef;

    constructor(public datePipe: DatePipe,
                public ajaxService: AjaxService,
                public router: Router,
                private activatedRoute: ActivatedRoute) {

    }

    public resizeCanvas(): void {
        this.signaturePad.set('canvasWidth', this.signatureContainer.nativeElement.clientWidth);
        this.signaturePad.set('canvasHeight', this.signatureContainer.nativeElement.clientHeight);
    }

    public ngAfterViewInit() {
        this.signatureContainer = this.signatureContainerQuery.find((sig, index) => index === 0);
        this.resizeCanvas();
    }

    ngOnInit(): void {
        this.claimId = this.activatedRoute.snapshot.params.claimId;
        this.formId = this.activatedRoute.snapshot.params.formId;
        this.userValues = this.activatedRoute.snapshot.data.userValues;
    }

    onSubmit(): void {
        console.log(this);
        this.userValues.values.date_signed =
            this.datePipe.transform(new Date(), 'MM/dd/yyyy');
        this.userValues.values.signature = this.signaturePad.toDataURL();

        this.ajaxService.saveForm(
            this.claimId,
            this.formId,
            this.userValues.values
        ).subscribe( (res) => {
            this.router.navigateByUrl('sign/' + this.claimId);
        })
    }

    clear(): void {
        this.signaturePad.clear();
    }

    @HostListener('window:resize')
    onResize() {
        this.resizeCanvas();
    }
}
