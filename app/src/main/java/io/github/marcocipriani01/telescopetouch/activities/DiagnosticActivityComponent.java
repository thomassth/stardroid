/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01) and the Sky Map Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.activities;

import dagger.Component;
import io.github.marcocipriani01.telescopetouch.ApplicationComponent;
import io.github.marcocipriani01.telescopetouch.inject.PerActivity;

/**
 * Created by johntaylor on 4/15/16.
 */
@PerActivity
@Component(modules = DiagnosticActivityModule.class, dependencies = ApplicationComponent.class)
public interface DiagnosticActivityComponent {
    void inject(DiagnosticActivity activity);
}