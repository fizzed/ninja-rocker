/*
 * Copyright 2016 Fizzed Inc.
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
package com.fizzed.ninja.rocker;

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import com.google.common.base.Optional;
import java.io.IOException;
import ninja.Context;
import ninja.Result;
import ninja.i18n.Lang;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NinjaRockerTemplateTest {
    
    @Test
    public void contextPath() throws Exception {
        final Lang ninjaLang = mock(Lang.class);
        final Context context = mock(Context.class);
        final Result result = mock(Result.class);
        
        when(context.getContextPath()).thenReturn("/context");
        when(ninjaLang.getLanguage(context, Optional.of(result))).thenReturn(Optional.of("en"));
        
        final NinjaRockerFactory ninjaRockerFactory = new NinjaRockerFactoryImpl();
        
        // bare minimum model + ninja template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new NinjaRockerTemplate(this) {
                    // anonymous initializer!
                    {
                        this.__internal.setCharset("UTF-8");
                        this.__internal.setContentType(ContentType.HTML);
                        this.__internal.setTemplatePackageName("test");
                        this.__internal.setTemplateName("test");
                        this.N = ninjaRockerFactory.create(null, null, null, ninjaLang, context, result);
                    }
                    
                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        this.__internal.writeValue(N.contextPath);
                    }
                };
            }
        };
        
        RockerOutput out = model.render();
        
        assertThat(out.toString(), is("/context"));
    }
    
}
