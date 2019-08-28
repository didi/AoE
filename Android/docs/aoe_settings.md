
## File Header

* Preferences | Editor | File and Code Templates

* Select `include` Tab

    * C File Header
        ```
        #set( $ORGANIZATION_NAME = "AoE")
        #set( $VERSION = "1.1.0")
        // Copyright $YEAR The $ORGANIZATION_NAME Authors
        //
        // Licensed under the Apache License, Version 2.0 (the "License");
        // you may not use this file except in compliance with the License.
        // You may obtain a copy of the License at
        //
        //      http://www.apache.org/licenses/LICENSE-2.0
        //
        // Unless required by applicable law or agreed to in writing, software
        // distributed under the License is distributed on an "AS IS" BASIS,
        // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        // See the License for the specific language governing permissions and
        // limitations under the License.

        /**
        * 
        *
        * @author ${USER}
        * @since ${VERSION}
        */
        ```
    * File Header
        ```
        #set( $VERSION = "1.1.0")
        /**
        * 
        *
        * @author ${USER}
        * @since ${VERSION}
        */
        ```

## Copyright
* Preferences | Editor | Copyright | Copyright Profiles
* `Add`
```
Copyright $today.year The AoE Authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```