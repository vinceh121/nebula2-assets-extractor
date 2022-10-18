# ---
# $parser:nbinscriptserver$ $class:nroot$
# ---
new n3dnode visual
	new n3dnode plane
		.txyz 1.0 -0.5 -1.5
		new nmeshnode mesh
			.setfilename "data:if_hilfe.n/plane.nvx"
		sel ..
		new nlinknode shader
			.settarget "/data/shaders/if_hilfe"
		sel ..
		new ntexarraynode tex
			.settexture 0 "data:if_hilfe.n/texturenone.ntx" "none"
		sel ..
	sel ..
sel ..
