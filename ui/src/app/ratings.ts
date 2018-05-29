import {Component, Input, OnInit} from '@angular/core';
import {RatingsService, UserSelection} from './ratingsService';
import {ActivatedRoute, Router} from '@angular/router';
import {FormGroup} from '@angular/forms';
import {FormlyFieldConfig} from '@ngx-formly/core';


const ratingsHome = `
<div id="vfi-ratings-home">
  <div class="container">
    <div class="row">
      <div class="col-sm-10">
        <h2>Disability Rating Calculator</h2>
        
      </div>
      <div class="col-sm-2 float-right">
          <p><b class="last-updated-statement">Rating info last updated:</b>May 2017</p>
          <p class="rating-info-disclaimer">All information on this page comes directly from CFR 38.</p>
      </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <hr/>
        </div>
    </div>
    
    <div class="row">
        <div class="col-sm-8">
            <h3>Step 1: Select your service connected disabilities</h3>
            <p>To begin click “Add Condition”. You will be able to browse from the list of VA covered conditions.</p>
            <p>
              You may add multiple conditions and we will calculate
              your combined rating using the VA formula.
            </p>
        </div>
        <div class="col-xl-4">
          <a class="btn" routerLink="/ratings/category">Add Condition</a>
        </div>
    </div>
  
    
    
      <table class="table rating-table">
        <thead>
            <tr>
                <th scope="col">VA Diagnosis Code</th>
                <th scope="col">Condition</th>
                <th scope="col">Severity</th>
                <th scope="col">Rating</th>
                <th scope="col"></th>
            </tr>
        </thead>
        <tbody>
            <p *ngIf="userSelections.length == 0" class="table-placeholder">
                No conditions selected. Please click "Add Condition" to browse the tree of conditions.
            </p>
            <tr *ngFor="let item of userSelections">
              <td>{{item.diagnosisCode}}</td>
              <td>{{item.diagnosis}}</td>
              <td>{{item.subdiagnosis}}</td>
              <td>{{item.rating}}</td>
              <td>
                <div class="remove-condition" (click)="removeSelection(item)"></div>
              </td>
            </tr>
        </tbody>
      </table>
      
      <div class="row">
        <div class="col-xl-6">
          <h3>Total Rating:</h3> <h2>{{userRating}} %</h2>
        </div>
        
      </div>
      
      
      <div class="row">
        <div class="col-sm-12">
            <hr/>
        </div>
    </div>
      
      <div class="row">
        <div class="col-sm-8">
            <h3>Step 2: Fill out your dependent information.</h3>
            <p>VA compensation is also based on the number of dependents you have.</p>
            <p>Answer the following questions so that we can calculate your monthly disability payments:</p>
        </div>
    </div>
   
      
       
      
      <form name="form" (ngSubmit)="submit()">
          <formly-form [form]="form" [model]="model" [fields]="fields">
          </formly-form>
      </form>
      <div class="row">
          <div class="col-xl-6">
              <h3>Monthly Compensation:</h3> <h2>{{calculateMoney() | currency:'USD'}}</h2>
          </div>
      </div>
  </div>
  
  
</div>`;

const fields: FormlyFieldConfig[] = [
    {
        fieldGroupClassName: 'row',
        fieldGroup: [
            {
                className: 'col-3',
                'key': 'has_children_y_n',
                'type': 'radio',
                'templateOptions': {
                    'label': 'Do you have children?',
                    'options': [
                        {
                            'key': 'Yes',
                            'label': 'Yes',
                            'value': 'Yes'
                        },
                        {
                            'key': 'No',
                            'label': 'No',
                            'value': 'No'
                        }
                    ]
                },
                'defaultValue': 'No'
            },
            {
                className: 'col-3',
                'key': 'num_under_18_children',
                'type': 'input',
                'templateOptions': {
                    'type': 'number',
                    'min': 0,
                    'label': 'How many children under 18 do you have?'
                },
                'defaultValue': 0,
                'hideExpression': 'model.has_children_y_n != \'Yes\''
            },
            {
                className: 'col-3',
                'key': 'num_over_18_children',
                'type': 'input',
                'templateOptions': {
                    'type': 'number',
                    'min': 0,
                    'label': 'How many children over 18 who are attending school/college?'
                },
                'defaultValue': 0,
                'hideExpression': 'model.has_children_y_n != \'Yes\''
            }
        ]
    },
    {
        fieldGroupClassName: 'row',
        fieldGroup: [
            {
                className: 'col-6',
                'key': 'has_parents_y_n',
                'type': 'radio',
                'templateOptions': {
                    'label': 'Do you have parents that are your dependents?',
                    'options': [
                        {
                            'key': 'Yes',
                            'label': 'Yes',
                            'value': 'Yes'
                        },
                        {
                            'key': 'No',
                            'label': 'No',
                            'value': 'No'
                        }
                    ]
                },
                'defaultValue': 'No'
            },
            {
                className: 'col-6',
                'key': 'num_parents',
                'type': 'radio',
                'templateOptions': {
                    'label': 'Do you have one or two parents as dependents?',
                    'options': [
                        {
                            'key': 'One',
                            'label': 'One',
                            'value': 'One'
                        },
                        {
                            'key': 'Two',
                            'label': 'Two',
                            'value': 'Two'
                        }
                    ]
                },
                'defaultValue': 0,
                'hideExpression': 'model.has_parents_y_n != \'Yes\''
            }
        ]
    },
    {
        fieldGroupClassName: 'row',
        fieldGroup: [
            {
                className: 'col-6',
                'key': 'has_spouse_y_n',
                'type': 'radio',
                'templateOptions': {
                    'label': 'Do you have a spouse?',
                    'options': [
                        {
                            'key': 'Yes',
                            'label': 'Yes',
                            'value': 'Yes'
                        },
                        {
                            'key': 'No',
                            'label': 'No',
                            'value': 'No'
                        }
                    ]
                },
                'defaultValue': 'No'
            },
            {
                className: 'col-6',
                'key': 'aid_and_attendance_spouse_y_n',
                'type': 'radio',
                'templateOptions': {
                    'label': 'Does your spouse require aid and attendance (blind, bedridden, or requiring help bathing, dressing, etc.)?',
                    'options': [
                        {
                            'key': 'Yes',
                            'label': 'Yes',
                            'value': 'Yes'
                        },
                        {
                            'key': 'No',
                            'label': 'No',
                            'value': 'No'
                        }
                    ]
                },
                'defaultValue': 'No',
                'hideExpression': 'model.has_spouse_y_n != \'Yes\''
            }
        ]
    }
];

@Component(
    {
        selector: 'ratings-home',
        template: ratingsHome,
        styleUrls: ['../assets/styles/ratingHome.styl']
    }
)
export class RatingsHome implements OnInit {

    public ratingsConfig: any[];
    public userRating: number;
    public userSelections: any[];
    public fields = fields;
    public model = {};
    public form = new FormGroup({});

    constructor(public ratingsService: RatingsService,
                public router: Router,
                public activatedRoute: ActivatedRoute) {

    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        this.userRating = this.ratingsService.getUserRating();
        this.userSelections = this.ratingsService.getSelections();
    }

    removeSelection(item: UserSelection) {
        this.ratingsService.removeSelection(item);
        this.userSelections = this.ratingsService.getSelections();
    }

    addCondition(item: UserSelection) {
        this.ratingsService.addSelection(item);
        this.router.navigateByUrl('');
    }

    submit() {
        console.log(this.form);
        console.log(this.fields);
        if (this.form.valid) {
            alert(JSON.stringify(this.model));
        }
    }

    calculateMoney() {
        return RatingsService.getCompensation(
            this.userRating,
            this.model);
    }
}


const ratingsSelect = `
<ratings-category-breadcrumbs [breadcrumbs]="breadcrumbs"></ratings-category-breadcrumbs>
<div id="vfi-ratings">
    <div class="container">

<div *ngIf="ratings.length > 0">
        <h3 class="section-header">Rated Conditions</h3>
        <div class="row">
          <div class="col-xl-12">
            <hr />
          </div>
        </div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>Description</th>
                <th>Rating</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr *ngFor="let rating of ratings">
                <td>{{rating.description}}</td>
                <td>{{rating.rating}}</td>
                <td>
                    <button (click)="addRating(rating)">Add</button>
                </td>
            </tr>
            </tbody>
        </table>
        </div>

        <div *ngIf="notes.length > 0">
        
            <h3 class="section-header">Notes</h3>
            <div class="row">
                <div class="col-xl-12">
                    <hr />
                </div>
            </div>
            <div class="row" *ngFor="let note of notes">
                <div class="col-sm-12">
                    <p>{{note.note}}</p>
                </div>
            </div>
        </div>

        <div *ngIf="see_other_notes.length > 0">
            <h3 class="section-header">See Other Conditions</h3>
            <div class="row">
                <div class="col-xl-12">
                    <hr />
                </div>
            </div>
            <div class="row" *ngFor="let note of see_other_notes">
                <div class="col-sm-12">{{note.see_other_note}}</div>
            </div>
        </div>
    </div>
</div>
`;

@Component(
    {
        selector: 'ratings-select',
        template: ratingsSelect,
        styleUrls: ['../assets/styles/ratingSelect.styl']
    }
)
export class RatingsSelect {

    public ratingsConfig: any;
    public ratings: any;
    public notes: any[];
    public category: any;
    public see_other_notes: any[];
    public breadcrumbs: string[];
    public code: any;


    constructor(public ratingsService: RatingsService,
                public activatedRoute: ActivatedRoute,
                public router: Router) {

    }

    unpackPath(rootCategory: any, path: number[]) {
        this.breadcrumbs = getBreadCrumbsFromPath(rootCategory, path);
        this.category = resolveCategoryFromPath(rootCategory, path);
        this.breadcrumbs.push(this.category.ratings[this.ratingPath()].code.description);
        this.ratings = this.category.ratings[this.ratingPath()].ratings;
        this.notes = this.category.ratings[this.ratingPath()].notes;
        this.see_other_notes = this.category.ratings[this.ratingPath()].see_other_notes;
        this.code = this.category.ratings[this.ratingPath()].code;
        console.log(this.category.ratings[this.ratingPath()]);
        console.log(this.see_other_notes);
    }

    ngOnInit(): void {
        this.ratingsConfig = this.activatedRoute.snapshot.data.ratingsConfig;
        let rootCategory = {
            description: 'All Categories',
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

    ratingPath(): number {
        return parseInt(this.activatedRoute.snapshot.params.ratingPath);
    }

    addRating(selection: any): void {
        this.ratingsService.addSelection(
            new UserSelection(
                selection.rating,
                this.code.description,
                selection.description,
                this.code.code));
        this.router.navigateByUrl('ratings');
    }
}


const ratingsCategories = `
<ratings-category-breadcrumbs [breadcrumbs]="breadcrumbs"></ratings-category-breadcrumbs>
<div id="vfi-ratings-categories">
    <div class="container">
      <h3 class="section-header">VA Covered Conditions Browser</h3>
      <div class="row">
         <div class="col-xl-12">
            <hr />
         </div>
      </div>
      <div *ngIf="ratings.length > 0" class="container">
        <h4 class="section-header">Conditions</h4>
        <div class="row">
          <div class="col-xl-12">
            <hr />
          </div>
        </div>
        
        <table class="table table-striped">
            <thead>
                    <tr>
                        <th>Condition Name</th>
                        <th>VA Condition Code</th>
                        <th></th>
                    </tr>
            </thead>
            <tbody>
                <tr *ngFor="let rating of ratings; index as i">
                    <td>{{rating.code.description}}</td>
                    <td>{{rating.code.code}}</td>
                    <td> 
                        <a class="btn" routerLink="/ratings/category/{{currentUrlPath}}/rating/{{i}}">
                            Select Condition
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>
      </div>
      
      <table *ngIf="notes.length > 0">
        <h4 class="section-header">Notes</h4>
        <div class="row">
          <div class="col-xl-12">
            <hr />
          </div>
        </div>
        <div *ngFor="let note of notes" class="row">
          {{note.note}}
        </div>
      </table>
      
      <div *ngIf="subcategories.length > 0">
          <h4 class="section-header">Categories</h4>
          <div class="row">
            <div class="col-xl-12">
              <hr />
            </div>
          </div>
          <div class="rating-category-select-button row"
               *ngFor="let subcategory of subcategories; index as i">
            <a routerLink="/ratings/category/{{getPathToSubcategory(i)}}"
               class="subcategory">{{subcategory}}</a>
          </div>
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
        template: ratingsCategories,
        styleUrls: ['../assets/styles/ratingCategories.styl']
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
        return this.activatedRoute.snapshot.params.categoryPath.split(',').map(
                (x) => parseInt(x)
            );
    }

    unpackPath(rootCategory: any, params: any) {
        let path =
            params.categoryPath ? this.categoryPath() : [];
        this.currentUrlPath =
            params.categoryPath ? params.categoryPath : '';

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
            description: 'All Categories',
            subcategories: this.ratingsConfig,
            notes: [],
            ratings: []
        };

        this.activatedRoute.params.subscribe(
            params => {
                this.unpackPath(rootCategory, params)
            }
        );

        this.unpackPath(rootCategory, this.activatedRoute.snapshot.params);
    }

    getPathToSubcategory(index: number) {
        let currentUrlPath =
            this.activatedRoute.snapshot.params.categoryPath ? this.categoryPath() : '';

        if (!currentUrlPath) {
            return String(index);
        } else {
            return currentUrlPath + ',' + index;
        }
    }

    selectCondition(selection: any): void {
        this.ratingsService.addSelection(selection);
        this.router.navigateByUrl('ratings');
    }
}


const breadcrumbTemplate = `
<div id="vfi-breadcrumbs">
  <div class="breadcrumbs-wrapper">
    <div class="link-wrapper" *ngFor="let link of links; index as i">
        <span [ngClass]="{'lighten': link.lighten}" *ngIf="i > 0">&gt;</span>
        <span [ngClass]="{'lighten': link.lighten, 'current': link.current}">{{link.title}}</span>
    </div>
  </div>
</div>
`;

@Component(
    {
        selector: 'ratings-category-breadcrumbs',
        template: breadcrumbTemplate,
        styleUrls: ['../assets/styles/ratingBreadcrumbs.styl']
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
