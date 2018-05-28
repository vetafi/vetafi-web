import {Injectable} from '@angular/core';
import {AjaxService} from './net';
import {Observable} from 'rxjs/Observable';
import {of} from 'rxjs/observable/of';
import {map} from 'rxjs/operators';


export class UserSelection {

    constructor(public rating: number,
                public diagnosis: string,
                public subdiagnosis: string,
                public diagnosisCode: number) {

    }
}

@Injectable()
export class RatingsService {

    userSelection: any[] = [];
    totalScore = 0;
    configuration: any = null;

    constructor(public ajaxService: AjaxService) {

    }

    static getScore(ratingSelections: UserSelection[]): number {
        console.log(ratingSelections);
        let scores = ratingSelections
            .map((selection) => selection.rating)
            .sort()
            .reverse();

        scores.unshift(0);

        let unrounded = Math.round(scores.reduce((left, right) => {
            return (((100 - left) / 100) * right) + left
        }));

        return Math.round(unrounded / 10) * 10;
    }

    getUserRating(): number {
        return RatingsService.getScore(this.userSelection);
    }

    addSelection(selection: UserSelection): void {
        console.log("addSelection");
        console.log(this.userSelection);
        this.userSelection.push(selection);
        this.totalScore = RatingsService.getScore(this.userSelection)
    }

    removeSelection(item: UserSelection): void {
        this.userSelection = this.userSelection.filter((iteree) => item != iteree);
        this.totalScore = RatingsService.getScore(this.userSelection)
    }

    getSelections(): UserSelection[] {
        console.log("getSelections");
        console.log(this.userSelection);
        return this.userSelection;
    }

    getConfiguration(): Observable<any> {
        if (this.configuration) {
            return of(this.configuration);
        } else {
            return this.ajaxService
                .getRatingsConfig()
                .pipe(
                    map((config) => {
                        this.configuration = config;
                        return config;
                    }, this)
                )
        }
    }
}

