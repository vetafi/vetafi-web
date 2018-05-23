import {Component, Input, OnInit} from '@angular/core';
import {RatingsService} from './ratingsService';
import {ActivatedRoute, Router} from '@angular/router';


const ratingsHome = `
<div id="vfi-ratings-home">
  <div class="container nopadding">
    <div class="row">
      <div class="col-sm-6">
        <h3>ProRate - Disability Rating and Pension Calculator</h3>
        <p>To begin click “Add Condition”.</p>
        <p>
          You may add multiple conditions and we will calculate
          your combined rating using the VA formula.
        </p>
      </div>
      <div class="col-sm-2">
        <div><span><b class="last-updated-statement">Rating info last updated:</b>May 2017</span>
          <p class="rating-info-disclaimer">All information on this page comes directly from CFR 38.</p>
        </div>
      </div>
    </div>
  </div>
  <div class="container nopadding">
    <div class="row rating-selection-header" [hidden]="userRating.ratingSelections.length > 0">
      <div class="col-sm-4">Condition</div>
      <div class="col-sm-2">Rating</div>
    </div>
    <div class="row rating-selection-row" *ngFor="let item of userRating.ratingSelections">
      <div class="col-sm-4">
        <p></p>{{item.diagnosis.code}}: {{item.diagnosis.description}}
      </div>
      <div class="col-sm-2">
        <p></p>{{item.rating}}
      </div>
      <div class="col-sm-1">
        <div class="remove-rating-selection" (click)="removeSelection(item)"></div>
      </div>
    </div>
    <div class="row">
      <div class="col-sm-6">
        <button (click)="addCondition(item)">Add Condition</button>
      </div>
    </div>
    <div class="row">
      <div class="col-sm-6">
        <p></p>Total Rating: {{userRating.totalScore}}
      </div>
    </div>
  </div>
</div>`;

@Component(
    {
        selector: 'ratings-home',
        template: ratingsHome
    }
)
export class RatingsHome implements OnInit {

    public ratingsConfig: any;
    public userRating: number;

    constructor(public ratingsService: RatingsService,
                public router: Router,
                public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        this.userRating = this.ratingsService.getUserRating();
    }

    removeSelection(item: any) {
        this.ratingsService.removeSelection(item);
    }

    addCondition(item: any) {
        this.ratingsService.addSelection(item);
        this.router.navigateByUrl('');
    }
}


const ratingsSelect = `
<rating-category-breadcrumbs breadcrumbs="breadcrumbs"></rating-category-breadcrumbs>
<div id="vfi-ratings">
  <div class="container nopadding" [hidden]="ratings.length == 0">
    <div class="row">
      <div class="col-sm-6"><b>Description</b></div>
      <div class="col-sm-2"><b>Rating</b></div>
    </div>
    <div class="row rating-selection-row" *ngFor="let rating of ratings">
      <div class="col-sm-6">{{rating.description}}</div>
      <div class="col-sm-1">{{rating.rating}}</div>
      <div class="col-sm-1">
        <button (click)="addRating(rating)">Add</button>
      </div>
    </div>
  </div>
  <div class="container nopadding" [hidden]="notes.length == 0">
    <div class="row">
      <div class="col-sm-6"><b>Notes</b></div>
    </div>
    <div class="row" *ngFor="let note of notes">
      <div class="col-sm-6">
        <p>{{note.note}}</p>
      </div>
    </div>
  </div>
  <div class="container nopadding" [hidden]="see_other_notes.length == 0">
    <div class="row">
      <div class="col-sm-6">See Other Notes</div>
    </div>
    <div class="row" *ngFor="let note of see_other_notes">
      <div class="col-sm-6">{{note.see_other_note}}</div>
    </div>
  </div>
</div>`;

@Component(
    {
        selector: 'ratings-select',
        template: ratingsSelect
    }
)
export class RatingsSelect {

    public ratingsConfig: any;

    constructor(public ratingsService: RatingsService,
                public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
    }
}


const ratingsCategories = `
<rating-category-breadcrumbs [breadcrumbs]="breadcrumbs"></rating-category-breadcrumbs>
<div id="vfi-ratings-categories">
  <h4>Select Condition</h4>
  <h5 [hidden]="subcategories.length == 0">Subcategories</h5>
  <div class="rating-category-select-button"
    *ngFor="let subcategory of subcategories; index as i"
    (click)="gotoSubcategory(i)">{{subcategory}}</div>
  <h5 [hidden]="ratings.length == 0">Conditions</h5>
  <div class="condition-select-button"
       *ngFor="let rating of ratings; index as i"
       (click)="gotoRating(i)">{{rating.code.description}} ({{rating.code.code}})</div>
  <table [hidden]="notes.length == 0">
    <thead>
      <tr>
        <th>Notes</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let note of notes">
        <td>{{note.note}}</td>
      </tr>
    </tbody>
  </table>
</div>`;

@Component(
    {
        selector: 'ratings-categories',
        template: ratingsCategories
    }
)
export class RatingsCategories {

    public breadcrumbs: string[];
    public notes: string[];
    public subcategories: string[];
    public category: any;
    public ratings: any[];
    public ratingsConfig: any;

    constructor(public ratingsService: RatingsService,
                public activatedRoute: ActivatedRoute) {

    }

    categoryPath() {
        return this.activatedRoute.snapshot.params.categoryPath.split(",").map(parseInt);
    }


    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;

        let path =
            this.activatedRoute.snapshot.params.categoryPath ? this.categoryPath() : [];

        let currentCategory = this.ratingsConfig;
        let tempBreadcrumbs = [];
        tempBreadcrumbs.push(currentCategory.description);

        path.forEach(
            (i) => {
                currentCategory = currentCategory.subcategories[i];
                tempBreadcrumbs.push(currentCategory.description);
            }
        );

        this.breadcrumbs = tempBreadcrumbs;
        this.notes = currentCategory.notes;
        this.category = currentCategory;

        this.subcategories = this.category.subcategories.map(
            (c) => { return c.description });
        this.category = this.category.description;
        this.ratings = this.category.ratings;
    }

    gotoSubcategory(i): void {

    }

    gotoRating(i): void {

    }
}


const breadcrumbTemplate = `
<div id="vfi-breadcrumbs">
  <div class="breadcrumbs-wrapper">
    <div class="link-wrapper" *ngFor="let link of links; index as i">
        <span [ngClass]="{'lighten': link.lighten}"
              *ngIf="i > 0"></span>
        <span [ngClass]="{'lighten': link.lighten, 'current': link.current}">{{link.title}}</span>
    </div>
  </div>
</div>
`;

@Component(
    {
        selector: 'ratings-category-breadcrumbs',
        template: breadcrumbTemplate
    }
)
export class RatingCategoryBreadcrumbs implements OnInit {
    @Input() breadcrumbs: any[];
    links: any[];

    ngOnInit(): void {
        this.links = this.breadcrumbs.map((title) => {
            return {title: title};
        });

        for (let i = 0; i < this.links.length - 1; i++) {
            this.links[i].lighten = true;
        }

        this.links[(this.links.length - 1)].current = true;
    }
}
