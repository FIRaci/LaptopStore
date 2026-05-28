const jwt = require("jsonwebtoken");

const JWT_SECRET = process.env.JWT_SECRET || "laptopstore_super_secret_key_2026";

function verifyToken(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({ error: "Access denied. No token provided." });
  }

  const token = authHeader.split(" ")[1];

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded; // { id, email, role }
    next();
  } catch (error) {
    res.status(401).json({ error: "Invalid token." });
  }
}

function isAdmin(req, res, next) {
  if (req.user && req.user.role === "ADMIN") {
    next();
  } else {
    res.status(403).json({ error: "Access denied. Admin only." });
  }
}

module.exports = {
  verifyToken,
  isAdmin,
  JWT_SECRET
};
