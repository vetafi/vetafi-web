import {Component, OnDestroy} from '@angular/core';
import {CookieService} from "ngx-cookie";
import {IntervalObservable} from "rxjs/observable/IntervalObservable";
import {Subscription} from "rxjs/subscription";

const INTERVAL = 100;
const COOKIE_NAME = 'fileDownloadToken';

const template = `
<div id="busy-overlay" *ngIf="show">
    <img class="busy-spinner" src="../icons/spinner.svg"/>
</div>
`;

@Component(
    {
        selector: 'app-busy-spinner',
        template: template,
        styleUrls: ['../assets/styles/busySpinner.styl']
    }
)
export class BusySpinnerComponent implements OnDestroy {


    cookieService;
    intervalSubscription: Subscription;
    show: boolean = false;

    constructor(cookieService: CookieService) {

    }

    downloadComplete() {
        return this.cookieService.getAll().hasOwnProperty(COOKIE_NAME)
    }

    showBusy() {
        this.show = true;
    }

    hideBusy() {
        this.show = false;
    }

    showBusyUntilDownload() {
        this.showBusy();

        if (this.intervalSubscription) {
            this.intervalSubscription.unsubscribe();
        }

        this.intervalSubscription = IntervalObservable.create(INTERVAL)
            .subscribe(
                () => {
                    if (this.downloadComplete()) {
                        this.cookieService.remove(COOKIE_NAME);
                        this.hideBusy();
                    }
                }
            );
    }

    ngOnDestroy(): void {
        if (this.intervalSubscription) {
            this.intervalSubscription.unsubscribe();
        }
    }
}
