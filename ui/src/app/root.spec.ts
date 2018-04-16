import {async, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientModule} from '@angular/common/http';

import {RootComponent} from './root';
import 'rxjs/add/observable/of';

describe('RootComponent', () => {
    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                RootComponent
            ],
            imports: [
                RouterTestingModule,
                HttpClientModule
            ]
        }).compileComponents();
    }));
    it('should create the app', async(() => {
        const fixture = TestBed.createComponent(RootComponent);
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    }));
});
