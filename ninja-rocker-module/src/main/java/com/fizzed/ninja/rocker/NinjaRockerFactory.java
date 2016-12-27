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
package com.fizzed.ninja.rocker;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import ninja.Context;
import ninja.Result;
import ninja.Router;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaProperties;

/**
 * Interface for factory that creates DefaultNinjaRocker instances.
 * 
 * @author joelauer
 */
@ImplementedBy(NinjaRockerFactoryImpl.class)
public interface NinjaRockerFactory {

    DefaultNinjaRocker create(Injector injector,
                              NinjaProperties ninjaProperties,
                              Router router,
                              Messages messages,
                              Lang ninjaLang,
                              Context context,
                              Result result);
    
}
