import {Injectable} from '@angular/core';
import {AjaxService} from './net';
import {Observable} from 'rxjs/Observable';

@Injectable()
export class RatingsService {

    userSelection: any[] = [];
    totalScore = 0;
    configuration: any = null;

    constructor(public ajaxService: AjaxService) {

    }

    static getScore(ratingSelections: any[]): number {
        let scores =  ratingSelections
            .map((selection) => selection.rating)
            .sort((selection) => selection.rating)
            .reverse();

        scores.unshift(0);

        return Math.round(scores.reduce((left, right) => {
            return (((100 - left) / 100) * right) + left
        }))
    }

    getUserRating(): number {
        return RatingsService.getScore(this.userSelection);
    }

    addSelection(selection: any): void {
        this.userSelection.push(selection);
        this.totalScore = RatingsService.getScore(this.userSelection)
    }

    removeSelection(item: any): void {
        this.userSelection = this.userSelection.filter((iteree) => item != iteree);
        this.totalScore = RatingsService.getScore(this.userSelection)
    }

    getSelections(): any[] {
        return this.userSelection;
    }

    getConfiguration(): Observable<any> {
        if (this.configuration) {
            return Observable.of(this.configuration)
        } else {
            return this.ajaxService
                .getRatingsConfig()
                .pipe((config) => {
                    this.configuration = config;
                    return config;
                })
        }
    }
}
