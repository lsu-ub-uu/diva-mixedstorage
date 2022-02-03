/*
 * Copyright 2021 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.mixedstorage.fedora;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.xmlutils.transformer.CoraTransformation;
import se.uu.ub.cora.xmlutils.transformer.CoraTransformationFactory;

public class TransformationFactorySpy implements CoraTransformationFactory {

	public CoraTransformationSpy transformationSpy;
	public String xsltPath;
	public List<String> xsltPaths = new ArrayList<>();
	public List<CoraTransformationSpy> factoredTransformations = new ArrayList<>();
	public String mainXsltPath;
	public String relatedXsltPath;

	@Override
	public CoraTransformation factor(String xsltPath) {
		xsltPaths.add(xsltPath);
		this.xsltPath = xsltPath;
		transformationSpy = new CoraTransformationSpy();
		factoredTransformations.add(transformationSpy);
		return transformationSpy;
	}

}
