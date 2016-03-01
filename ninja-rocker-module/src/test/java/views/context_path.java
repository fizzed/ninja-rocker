/*
 * Copyright 2016 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package views;

import com.fizzed.ninja.rocker.NinjaRockerTemplate;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import java.io.IOException;

public class context_path extends DefaultRockerModel {
    @Override
    protected DefaultRockerTemplate buildTemplate() throws RenderingException {
        return new NinjaRockerTemplate(this) {
            // anonymous initializer!
            {
                this.__internal.setCharset("UTF-8");
                this.__internal.setContentType(ContentType.HTML);
                this.__internal.setTemplatePackageName("views");
                this.__internal.setTemplateName("context_path");
            }

            @Override
            protected void __doRender() throws IOException, RenderingException {
                this.__internal.writeValue(N.contextPath);
            }
        };
    }
}
