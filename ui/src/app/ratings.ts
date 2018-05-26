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
    <div class="row rating-selection-header" [hidden]="userSelections.length > 0">
      <div class="col-sm-4">Condition</div>
      <div class="col-sm-2">Rating</div>
    </div>
    <div class="row rating-selection-row" *ngFor="let item of userSelections">
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
        <a class="btn" routerLink="/ratings/category">Add Condition</a>
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

    public ratingsConfig: any[];
    public userRating: number;
    public userSelections: any[];

    constructor(public ratingsService: RatingsService,
                public router: Router,
                public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        this.userRating = this.ratingsService.getUserRating();
        this.userSelections = this.ratingsService.getSelections();
        console.log(this.userSelections);
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
<ratings-category-breadcrumbs [breadcrumbs]="breadcrumbs"></ratings-category-breadcrumbs>
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
    public ratings: any[];
    public notes: any[];
    public see_other_notes: any[];
    public breadcrumbs: string[];
    public category: any;


    constructor(public ratingsService: RatingsService,
                public activatedRoute: ActivatedRoute) {

    }

    unpackPath(rootCategory: any, path: number[]) {
        this.breadcrumbs = getBreadCrumbsFromPath(rootCategory, path);
        this.category = resolveCategoryFromPath(rootCategory, path);
        this.notes = this.category.notes;
        this.ratings = this.category.ratings;
    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        let rootCategory = {
            description: "All Categories",
            subcategories: this.ratingsConfig,
            notes: [],
            ratings: []
        };

        let categoryPath =
            this.activatedRoute.snapshot.params.categoryPath ? this.categoryPath() : [];
        let ratingPath =
            this.activatedRoute.snapshot.params.categoryPath ? this.ratingPath() : [];

        this.unpackPath(rootCategory, categoryPath);

    }

    categoryPath(): number[] {
        return this.activatedRoute.snapshot.params.categoryPath.split(',').map(parseInt);
    }

    ratingPath(): number[] {
        return this.activatedRoute.snapshot.params.ratingPath.split(',').map(parseInt);
    }
}


const ratingsCategories = `
<ratings-category-breadcrumbs [breadcrumbs]="breadcrumbs"></ratings-category-breadcrumbs>
<div id="vfi-ratings-categories">
  <h4>Select Condition</h4>
  <div *ngIf="ratings.length > 0" class="container">
    <h5>Conditions</h5>
    
    <div class="row">
        <div class="col-xl-3">Condition Name</div>
        <div class="col-xl-3">VA Condition Code</div>
    </div>
    <div class="row" *ngFor="let rating of ratings; index as i">
        <div class="col-xl-3">{{rating.code.description}}</div>
        <div class="col-xl-3">{{rating.code.code}}</div>
        <div class="col-xl-3">
            <a class="btn" routerLink="/ratings/category/{{currentUrlPath}}/rating/{{i}}">
                Select Condition
            </a>
        </div>
    </div>
  </div>
  
  <table *ngIf="notes.length > 0">
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
  
  <div *ngIf="subcategories.length > 0">
      <h5>Subcategories</h5>
      <div class="rating-category-select-button"
           *ngFor="let subcategory of subcategories; index as i">
        <a routerLink="/ratings/category/{{getPathToSubcategory(i)}}">{{subcategory}}</a>
      </div>
  </div>
</div>`;


function getBreadCrumbsFromPath(rootCategory: any, path: number[]) {
    let currentCategory = rootCategory;
    let tempBreadcrumbs = [];
    tempBreadcrumbs.push(currentCategory.description);

    path.forEach(
        (i) => {
            currentCategory = currentCategory.subcategories[i];
            tempBreadcrumbs.push(currentCategory.description);
        }
    );

    return tempBreadcrumbs;
}

function resolveCategoryFromPath(rootCategory: any, path: number[]) {
    let currentCategory = rootCategory;

    path.forEach(
        (i) => {
            currentCategory = currentCategory.subcategories[i];
        }
    );

    return currentCategory;
}

@Component(
    {
        selector: 'ratings-categories',
        template: ratingsCategories
    }
)
export class RatingsCategories {

    public breadcrumbs: string[] = [];
    public notes: string[] = [];
    public subcategories: string[] = [];
    public ratings: any[] = [];
    public ratingsConfig: any;
    public category: any;
    public currentUrlPath: string;

    constructor(public ratingsService: RatingsService,
                public activatedRoute: ActivatedRoute,
                public router: Router) {

    }

    categoryPath(): number[] {
        return this.activatedRoute.snapshot.params.categoryPath.split(',').map(parseInt);
    }

    unpackPath(rootCategory: any, path: number[]) {
        this.breadcrumbs = getBreadCrumbsFromPath(rootCategory, path);
        this.category = resolveCategoryFromPath(rootCategory, path);
        this.notes = this.category.notes;
        this.subcategories = this.category.subcategories.map(
            (c) => {
                return c.description;
            });
        this.ratings = this.category.ratings;
        console.log(this.category);
    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        let rootCategory = {
            description: "All Categories",
            subcategories: this.ratingsConfig,
            notes: [],
            ratings: []
        };

        let path =
            this.activatedRoute.snapshot.params.categoryPath ? this.categoryPath() : [];
        this.currentUrlPath =
            this.activatedRoute.snapshot.params.categoryPath ?
                this.activatedRoute.snapshot.params.categoryPath : "";

        this.unpackPath(rootCategory, path);
    }

    getPathToSubcategory(index: number) {
        let currentUrlPath =
            this.activatedRoute.snapshot.params.categoryPath ? this.categoryPath() : "";

        if (!currentUrlPath) {
            return String(index);
        } else {
            return currentUrlPath + "," + index;
        }
    }

    selectCondition(selection: any): void {
        this.ratingsService.addSelection(selection);
        this.router.navigateByUrl("ratings")
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
