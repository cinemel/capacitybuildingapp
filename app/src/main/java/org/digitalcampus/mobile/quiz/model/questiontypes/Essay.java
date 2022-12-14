/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.mobile.quiz.model.questiontypes;

import java.io.Serializable;

public class Essay extends UserResponseQuestion implements Serializable {

    private static final long serialVersionUID = 1531985882092686497L;
    public static final String TAG = Essay.class.getSimpleName();

    @Override
    public void mark(String lang) {
        this.userscore = 0;
    }

    @Override
    public boolean isUserInputResponse(){ return true; }
}