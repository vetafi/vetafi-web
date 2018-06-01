import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AjaxService} from './net';
import {TestMessage} from './models/test_pb';

const template = `
<h3>thing</h3>
<div>
{{message | json}}
</div>
<b>{{thing}}</b>
`;

@Component({
    selector: 'test',
    template: template
})
export class TestComponent implements OnInit {

    public message: TestMessage.AsObject;
    public thing: string;

    constructor(public ajaxService: AjaxService) {

    }

    ngOnInit() {
        this.ajaxService.test().subscribe(
            (response: TestMessage.AsObject) => {
                console.log(response);
                console.log(typeof response);
                this.message = response;
                this.thing = response.stringfield;
            }
        )
    }


}
