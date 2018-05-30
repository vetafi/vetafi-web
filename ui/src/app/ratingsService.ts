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

const rates = {
    "10": {
        "Dependent Status": "10",
        "Veteran Alone": 136.24,
        "Veteran with Spouse Only": 136.24,
        "Veteran with Spouse & One Parent": 136.24,
        "Veteran with Spouse and Two Parents": 136.24,
        "Veteran with One Parent": 136.24,
        "Veteran with Two Parents": 136.24,
        "Additional for A/A spouse": 0.00,
        "Veteran with Spouse and Child": 136.24,
        "Veteran with Child Only": 136.24,
        "Veteran with Spouse, One Parent and Child": 136.24,
        "Veteran with Spouse, Two Parents and Child": 136.24,
        "Veteran with One Parent and Child": 136.24,
        "Veteran with Two Parents and Child": 136.24,
        "Add for Each Additional Child Under Age 18": 0.00,
        "Each Additional Schoolchild Over Age 18": 0.00
    },
    "20": {
        "Dependent Status": "20",
        "Veteran Alone": 269.30,
        "Veteran with Spouse Only": 269.30,
        "Veteran with Spouse & One Parent": 269.30,
        "Veteran with Spouse and Two Parents": 269.30,
        "Veteran with One Parent": 269.30,
        "Veteran with Two Parents": 269.30,
        "Additional for A/A spouse": 0.00,
        "Veteran with Spouse and Child": 269.30,
        "Veteran with Child Only": 269.30,
        "Veteran with Spouse, One Parent and Child": 269.30,
        "Veteran with Spouse, Two Parents and Child": 269.30,
        "Veteran with One Parent and Child": 269.30,
        "Veteran with Two Parents and Child": 269.30,
        "Add for Each Additional Child Under Age 18": 0.00,
        "Each Additional Schoolchild Over Age 18": 0.00
    },
    "30": {
        "Dependent Status": "30",
        "Veteran Alone": 417.15,
        "Veteran with Spouse Only": 466.15,
        "Veteran with Spouse & One Parent": 505.15,
        "Veteran with Spouse and Two Parents": 544.15,
        "Veteran with One Parent": 456.15,
        "Veteran with Two Parents": 495.15,
        "Additional for A/A spouse": 46.00,
        "Veteran with Spouse and Child": 503.15,
        "Veteran with Child Only": 450.15,
        "Veteran with Spouse, One Parent and Child": 542.15,
        "Veteran with Spouse, Two Parents and Child": 581.15,
        "Veteran with One Parent and Child": 489.15,
        "Veteran with Two Parents and Child": 528.15,
        "Add for Each Additional Child Under Age 18": 24.00,
        "Each Additional Schoolchild Over Age 18": 79.00
    },
    "40": {
        "Dependent Status": "40",
        "Veteran Alone": 600.90,
        "Veteran with Spouse Only": 666.90,
        "Veteran with Spouse & One Parent": 719.90,
        "Veteran with Spouse and Two Parents": 772.90,
        "Veteran with One Parent": 653.90,
        "Veteran with Two Parents": 706.90,
        "Additional for A/A spouse": 61.00,
        "Veteran with Spouse and Child": 714.90,
        "Veteran with Child Only": 644.90,
        "Veteran with Spouse, One Parent and Child": 767.90,
        "Veteran with Spouse, Two Parents and Child": 820.90,
        "Veteran with One Parent and Child": 697.90,
        "Veteran with Two Parents and Child": 750.90,
        "Add for Each Additional Child Under Age 18": 32.00,
        "Each Additional Schoolchild Over Age 18": 106.00
    },
    "50": {
        "Dependent Status": "50",
        "Veteran Alone": 855.41,
        "Veteran with Spouse Only": 937.41,
        "Veteran with Spouse & One Parent": 1003.41,
        "Veteran with Spouse and Two Parents": 1069.41,
        "Veteran with One Parent": 921.41,
        "Veteran with Two Parents": 987.41,
        "Additional for A/A spouse": 76.00,
        "Veteran with Spouse and Child": 998.41,
        "Veteran with Child Only": 910.41,
        "Veteran with Spouse, One Parent and Child": 1064.41,
        "Veteran with Spouse, Two Parents and Child": 1130.41,
        "Veteran with One Parent and Child": 976.41,
        "Veteran with Two Parents and Child": 1042.41,
        "Add for Each Additional Child Under Age 18": 41.00,
        "Each Additional Schoolchild Over Age 18": 133.00
    },
    "60": {
        "Dependent Status": "60",
        "Veteran Alone": 1083.52,
        "Veteran with Spouse Only": 1182.52,
        "Veteran with Spouse & One Parent": 1261.52,
        "Veteran with Spouse and Two Parents": 1340.52,
        "Veteran with One Parent": 1162.52,
        "Veteran with Two Parents": 1241.52,
        "Additional for A/A spouse": 91.00,
        "Veteran with Spouse and Child": 1255.52,
        "Veteran with Child Only": 1149.52,
        "Veteran with Spouse, One Parent and Child": 1334.52,
        "Veteran with Spouse, Two Parents and Child": 1413.52,
        "Veteran with One Parent and Child": 1228.52,
        "Veteran with Two Parents and Child": 1307.52,
        "Add for Each Additional Child Under Age 18": 49.00,
        "Each Additional Schoolchild Over Age 18": 159.00
    },
    "70": {
        "Dependent Status": "70",
        "Veteran Alone": 1365.48,
        "Veteran with Spouse Only": 1481.48,
        "Veteran with Spouse & One Parent": 1574.48,
        "Veteran with Spouse and Two Parents": 1667.48,
        "Veteran with One Parent": 1458.48,
        "Veteran with Two Parents": 1551.48,
        "Additional for A/A spouse": 106.00,
        "Veteran with Spouse and Child": 1566.48,
        "Veteran with Child Only": 1442.48,
        "Veteran with Spouse, One Parent and Child": 1659.48,
        "Veteran with Spouse, Two Parents and Child": 1752.48,
        "Veteran with One Parent and Child": 1535.48,
        "Veteran with Two Parents and Child": 1628.48,
        "Add for Each Additional Child Under Age 18": 57.00,
        "Each Additional Schoolchild Over Age 18": 186.00
    },
    "80": {
        "Dependent Status": "80",
        "Veteran Alone": 1587.25,
        "Veteran with Spouse Only": 1719.25,
        "Veteran with Spouse & One Parent": 1825.25,
        "Veteran with Spouse and Two Parents": 1931.25,
        "Veteran with One Parent": 1693.25,
        "Veteran with Two Parents": 1799.25,
        "Additional for A/A spouse": 122.00,
        "Veteran with Spouse and Child": 1816.25,
        "Veteran with Child Only": 1675.25,
        "Veteran with Spouse, One Parent and Child": 1922.25,
        "Veteran with Spouse, Two Parents and Child": 2028.25,
        "Veteran with One Parent and Child": 1781.25,
        "Veteran with Two Parents and Child": 1887.25,
        "Add for Each Additional Child Under Age 18": 65.00,
        "Each Additional Schoolchild Over Age 18": 212.00
    },
    "90": {
        "Dependent Status": "90",
        "Veteran Alone": 1783.68,
        "Veteran with Spouse Only": 1932.68,
        "Veteran with Spouse & One Parent": 2051.68,
        "Veteran with Spouse and Two Parents": 2170.68,
        "Veteran with One Parent": 1902.68,
        "Veteran with Two Parents": 2021.68,
        "Additional for A/A spouse": 137.00,
        "Veteran with Spouse and Child": 2041.68,
        "Veteran with Child Only": 1882.68,
        "Veteran with Spouse, One Parent and Child": 2160.68,
        "Veteran with Spouse, Two Parents and Child": 2279.68,
        "Veteran with One Parent and Child": 2001.68,
        "Veteran with Two Parents and Child": 2120.68,
        "Add for Each Additional Child Under Age 18": 74.00,
        "Each Additional Schoolchild Over Age 18": 239.00
    },
    "100": {
        "Dependent Status": "100",
        "Veteran Alone": 2973.86,
        "Veteran with Spouse Only": 3139.67,
        "Veteran with Spouse & One Parent": 3272.73,
        "Veteran with Spouse and Two Parents": 3405.79,
        "Veteran with One Parent": 3106.92,
        "Veteran with Two Parents": 3239.98,
        "Additional for A/A spouse": 152.06,
        "Veteran with Spouse and Child": 3261.10,
        "Veteran with Child Only": 3084.75,
        "Veteran with Spouse, One Parent and Child": 3394.16,
        "Veteran with Spouse, Two Parents and Child": 3527.22,
        "Veteran with One Parent and Child": 3217.81,
        "Veteran with Two Parents and Child": 3350.87,
        "Add for Each Additional Child Under Age 18": 82.38,
        "Each Additional Schoolchild Over Age 18": 266.13
    }
};

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
    
    static addAdditionalChildCredit(base: number,
                                    dependentInfo: any,
                                    ratingSpecificRates: any): number {
        base += ratingSpecificRates["Add for Each Additional Child Under Age 18"] *
            Math.max(dependentInfo.num_under_18_children - 1, 0);

        base += ratingSpecificRates["Each Additional Schoolchild Over Age 18"] *
            Math.max(dependentInfo.num_over_18_children - 1, 0);
        
        return base;
    }

    static getCompensation(rating: number, dependentInfo: any): number {
        let ratingKey = Math.round(rating).toString();

        if (!rates[ratingKey]) {
            return 0.0;
        }

        if (!dependentInfo.has_children_y_n) {
            dependentInfo.has_children_y_n = "No"
        }

        if (!dependentInfo.has_spouse_y_n) {
            dependentInfo.has_spouse_y_n = "No"
        }

        if (!dependentInfo.has_parents_y_n) {
            dependentInfo.has_parents_y_n = "No"
        }

        if (!dependentInfo.aid_and_attendance_spouse_y_n) {
            dependentInfo.aid_and_attendance_spouse_y_n = "No"
        }

        if (!dependentInfo.num_parents) {
            dependentInfo.num_parents = "One"
        }

        if (!dependentInfo.num_under_18_children) {
            dependentInfo.num_under_18_children = 0
        }

        if (!dependentInfo.num_over_18_children) {
            dependentInfo.num_over_18_children = 0
        }

        let ratingSpecificRates = rates[ratingKey];

        if (dependentInfo.has_children_y_n === "No" &&
            dependentInfo.has_spouse_y_n === "No" &&
            dependentInfo.has_parents_y_n === "No") {
            return ratingSpecificRates["Veteran Alone"];

        } else if (
            dependentInfo.has_children_y_n === "No" &&
            dependentInfo.has_spouse_y_n === "Yes" &&
            dependentInfo.has_parents_y_n === "No") {
            
            if (dependentInfo.aid_and_attendance_spouse_y_n === "Yes") {
                return ratingSpecificRates["Additional for A/A spouse"] +
                    ratingSpecificRates["Veteran with Spouse Only"];
            } else {
                return ratingSpecificRates["Veteran with Spouse Only"];
            }
            
        } else if (
            dependentInfo.has_children_y_n === "No" &&
            dependentInfo.has_spouse_y_n === "Yes" &&
            dependentInfo.has_parents_y_n === "Yes") {
            
            let base = 0;
            
            if (dependentInfo.num_parents === "One") {
                base = ratingSpecificRates["Veteran with Spouse & One Parent"];
            } else {
                base = ratingSpecificRates["Veteran with Spouse and Two Parents"];
            }

            if (dependentInfo.aid_and_attendance_spouse_y_n === "Yes") {
                return ratingSpecificRates["Additional for A/A spouse"] + base;
            } else {
                return base;
            }
            
        } else if (
            dependentInfo.has_children_y_n === "No" &&
            dependentInfo.has_spouse_y_n === "No" &&
            dependentInfo.has_parents_y_n === "Yes") {

            if (dependentInfo.num_parents === "One") {
                return ratingSpecificRates["Veteran with One Parent"];
            } else {
                return ratingSpecificRates["Veteran with Two Parents"];
            }
            
        } else if (
            dependentInfo.has_children_y_n === "Yes" &&
            dependentInfo.has_spouse_y_n === "Yes" &&
            dependentInfo.has_parents_y_n === "Yes") {
            
            let base = 0;
            
            if (dependentInfo.num_parents === "One") {
                base = ratingSpecificRates["Veteran with Spouse, One Parent and Child"];
            } else {
                base = ratingSpecificRates["Veteran with Spouse, Two Parents and Child"];
            }

            base = RatingsService.addAdditionalChildCredit(
                base,
                dependentInfo,
                ratingSpecificRates);


            if (dependentInfo.aid_and_attendance_spouse_y_n === "Yes") {
                base += ratingSpecificRates["Additional for A/A spouse"];
            }
            
            return base;
        } else if (
            dependentInfo.has_children_y_n == "Yes" &&
            dependentInfo.has_spouse_y_n === "Yes" &&
            dependentInfo.has_parents_y_n === "No") {
            
            let base = ratingSpecificRates["Veteran with Spouse and Child"];

            base = RatingsService.addAdditionalChildCredit(
                base,
                dependentInfo,
                ratingSpecificRates);

            if (dependentInfo.aid_and_attendance_spouse_y_n === "Yes") {
                base += ratingSpecificRates["Additional for A/A spouse"];
            }
            
            return base;
            
        } else if (
            dependentInfo.has_children_y_n == "Yes" &&
            dependentInfo.has_spouse_y_n === "No" &&
            dependentInfo.has_parents_y_n === "No") {

            let base = ratingSpecificRates["Veteran with Child Only"];

            return RatingsService.addAdditionalChildCredit(
                base,
                dependentInfo,
                ratingSpecificRates);
        } else if (
            dependentInfo.has_children_y_n == "Yes" &&
            dependentInfo.has_spouse_y_n === "No" &&
            dependentInfo.has_parents_y_n === "Yes") {
            let base = 0;

            if (dependentInfo.num_parents === "One") {
                base = ratingSpecificRates["Veteran with One Parent and Child"];
            } else {
                base = ratingSpecificRates["Veteran with Two Parents and Child"];
            }

            return RatingsService.addAdditionalChildCredit(
                base,
                dependentInfo,
                ratingSpecificRates);
        }


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

