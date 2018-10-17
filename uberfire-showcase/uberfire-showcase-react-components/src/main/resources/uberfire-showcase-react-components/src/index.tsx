import * as React from "react";
import * as AppFormer from 'appformer-js';
import {Clock} from "./Clock";
import {Files} from "./Files";

export class StaticReactComponent extends AppFormer.Screen {
    constructor() {
        super();
        this.isReact = true;
        this.af_componentId = "ReactComponent";
        this.af_componentTitle = "React component";
        this.af_subscriptions = {};
        this.af_componentService = {};
    }

    af_componentRoot(root?: { ss: AppFormer.Screen[]; ps: AppFormer.Perspective[] }): AppFormer.Element {
        return <div style={{padding: "10px"}}>
            <Clock/>
            <Files/>
        </div>;
    }

    af_onOpen(): void {
        console.info("ReactComponent is open.")
    }

    af_onClose(): void {
        console.info("ReactComponent is closed.")
    }
}

AppFormer.register({StaticReactComponent});