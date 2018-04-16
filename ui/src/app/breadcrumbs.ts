import {Component, Input, OnInit} from '@angular/core';

const template = `
<div id="app-breadcrumbs">
  <div class="breadcrumbs-wrapper">
    <div class="link-wrapper" *ngFor="let link of links; index as i;">
        <span [ngClass]="{'lighten': link.lighten}" *ngIf="i > 0">&gt;</span>
        <span [ngClass]="{'lighten': link.lighten, 'current': link.current}">{{link.title}}</span>
    </div>
  </div>
</div>
`;

@Component({
    selector: 'app-breadcrumbs',
    template: template,
    styleUrls: ['../assets/styles/breadcrumbs.styl']
})
export class BreadcrumbsComponent implements OnInit {

    @Input('page') page: string;
    links: Object[] = [
        {
            title: 'Start'
        },
        {
            title: 'Fill Forms'
        },
        {
            title: 'Sign'
        },
        {
            title: 'Review'
        },
        {
            title: 'Submit'
        }
    ];

    ngOnInit(): void {
        console.log(this);
        let pageNum = Number(this.page);
        pageNum = Math.min(pageNum, this.links.length - 1);
        pageNum = Math.max(pageNum, 0);

        for (let i = 0; i <= pageNum; i++) {
            this.links[i]['lighten'] = true;
        }
        this.links[pageNum]['current'] = true;

    }
}
