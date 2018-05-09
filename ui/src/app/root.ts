import {Component, EventEmitter, Output} from '@angular/core';


const template = `
<router-outlet></router-outlet>
`;

@Component({
    selector: 'app-root',
    template: template
})
export class RootComponent {

}
